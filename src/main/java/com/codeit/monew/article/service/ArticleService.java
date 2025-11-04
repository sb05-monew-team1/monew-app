package com.codeit.monew.article.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.activity.service.UserActivityService;
import com.codeit.monew.article.domain.Article;
import com.codeit.monew.article.domain.ArticleSource;
import com.codeit.monew.article.domain.ArticleView;
import com.codeit.monew.article.dto.ArticleBackupDto;
import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleRestoreResultDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.dto.ArticleSearchRequestFromService;
import com.codeit.monew.article.dto.ArticleSearchResultDto;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.exception.ArticleNotFoundException;
import com.codeit.monew.article.exception.ArticleViewAlreadyExistException;
import com.codeit.monew.article.mapper.ArticleMapper;
import com.codeit.monew.article.mapper.ArticleViewMapper;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.common.exception.storage.StorageException;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final PageResponseMapper pageResponseMapper;
	private final UserRepository userRepository;
	private final ArticleViewRepository articleViewRepository;
	private final ArticleStorage articleStorage;
	private final UserActivityService userActivityService;
	private final ObjectMapper objectMapper;

	private final ArticleMapper articleMapper;
	private final ArticleViewMapper articleViewMapper;

	@Transactional(readOnly = true)
	public CursorPageResponse<ArticleDto> search(ArticleSearchRequest request) {
		validateUser(request.monewRequestUserId());

		log.info("기사 검색 시작 userId={} orderBy={} direction={} cursor={} limit={}",
			request.monewRequestUserId(), request.orderBy(), request.direction(), request.cursor(), request.limit());

		ArticleSearchRequestFromService serviceRequest = ArticleSearchRequestFromService.from(request);

		ArticleSearchResultDto search = articleRepository.search(serviceRequest);
		Slice<ArticleDto> slice = search.slice();
		String nextCursor = null;
		Instant nextAfter = null;

		if (slice.hasNext() && slice.getNumberOfElements() > 0) {
			ArticleDto last = slice.getContent().get(slice.getNumberOfElements() - 1);

			nextAfter = search.createdAt();
			String baseCursor = switch (request.orderBy()) {
				case "commentCount" -> String.valueOf(last.commentCount());
				case "viewCount" -> String.valueOf(last.viewCount());
				default -> String.valueOf(last.publishDate());
			};
			if (nextAfter != null) {
				nextCursor = baseCursor + "|" + nextAfter;
			} else {
				nextCursor = baseCursor;
			}
		}

		long totalElements = articleRepository.count();
		log.debug("기사 검색 완료 userId={} 결과수={} 다음페이지여부={} nextCursor={} 전체건수={}",
			request.monewRequestUserId(), slice.getNumberOfElements(), slice.hasNext(), nextCursor, totalElements);

		return pageResponseMapper.toCursorPageResponse(slice, nextCursor, nextAfter, totalElements);
	}

	@Transactional(readOnly = true)
	public ArticleDto search(UUID articleId, UUID userId) {
		log.info("기사 단건 조회 articleId={} userId={}", articleId, userId);

		Article article = validateArticle(articleId);
		validateUser(userId);

		boolean viewedByMe = articleViewRepository.existsByUserIdAndArticleId(userId, articleId);
		log.debug("기사 단건 조회 결과 articleId={} viewedByMe={}", articleId, viewedByMe);

		return articleMapper.toArticleDto(article, viewedByMe);
	}

	@Transactional
	public ArticleViewDto registerArticleView(UUID articleId, UUID userId) {
		log.info("기사 뷰 등록 articleId={} userId={}", articleId, userId);

		Article article = validateArticle(articleId);
		User user = validateUser(userId);

		if (articleViewRepository.existsByUserIdAndArticleId(userId, articleId)) {
			log.info("이미 등록된 기사 뷰 articleId={} userId={}", articleId, userId);
			throw new ArticleViewAlreadyExistException().addDetail("articleId", articleId).addDetail("userId", userId);
		}

		ArticleView articleView = ArticleView.builder()
			.user(user)
			.article(article)
			.build();
		articleViewRepository.save(articleView);
		ArticleViewDto dto = articleViewMapper.toDto(articleView);

		userActivityService.deleteUserActivity(user.getId());
		log.debug("기사 뷰 등록 완료 articleViewId={} articleId={} userId={}", articleView.getId(), articleId, userId);

		return dto;
	}

	@Transactional(readOnly = true)
	public List<String> getSources() {
		log.debug("기사 출처 목록 조회");
		return List.of(Arrays.stream(ArticleSource.values()).map(Enum::name).toArray(String[]::new));
	}

	@Transactional
	public void deleteSoft(UUID articleId) {
		log.info("기사 논리 삭제 처리 articleId={}", articleId);

		Article article = validateArticle(articleId);
		article.deleteSoft(Instant.now());
		log.debug("기사 논리 삭제 완료 articleId={}", articleId);
	}

	@Transactional
	public void deleteHard(UUID articleId) {
		log.info("기사 물리 삭제 처리 articleId={}", articleId);

		// validateArticle 메소드의 경우 논리 삭제된 기사까지 검증하기 때문에 물리 삭제에선 검증 로직 따로 작성
		if (!articleRepository.existsById(articleId)) {
			log.warn("물리 삭제 대상 기사를 찾을 수 없음 articleId={}", articleId);
			throw new ArticleNotFoundException().addDetail("articleId", articleId);
		}

		articleRepository.deleteById(articleId);
		log.debug("기사 물리 삭제 완료 articleId={}", articleId);
	}

	@Transactional
	public ArticleRestoreResultDto restore(LocalDateTime from, LocalDateTime to) {
		ZoneId zone = ZoneId.of("Asia/Seoul");
		LocalDateTime restoreDate = LocalDateTime.now();

		log.info("기사 백업 복원 시작 from={} to={}", from, to);

		List<UUID> restoredIds = new ArrayList<>();
		Set<String> sourceUrls = articleRepository.findAllSourceUrls();

		for (LocalDateTime i = from; !i.isAfter(to); i = i.plusDays(1)) {
			InputStream stream = null;
			try {
				stream = articleStorage.get(i.atZone(zone).format(DateTimeFormatter.ISO_LOCAL_DATE));
			} catch (S3Exception e) {
				log.warn("S3 백업 파일 불러오기 실패 date={}", i, e);
			}
			if (stream == null) {
				log.debug("백업 파일이 존재하지 않음 date={}", i);
				continue;
			}
			List<Article> articles;
			try {
				articles = readBackup(stream, sourceUrls);
			} catch (IOException e) {
				throw new StorageException(ErrorCode.ARTICLE_RESTORE_FAILED, e);
			}
			log.debug("기사 복원 처리 건수={} date={}", articles.size(), i);
			articleRepository.saveAll(articles);
			restoredIds.addAll(articles.stream().map(Article::getId).toList());
		}

		return ArticleRestoreResultDto.builder()
			.restoreDate(restoreDate)
			.restoredArticleIds(List.copyOf(restoredIds))
			.restoredArticleCount(restoredIds.size())
			.build();
	}

	private Article validateArticle(UUID articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> new ArticleNotFoundException().addDetail("articleId", articleId));
		if (article.getDeletedAt() != null) {
			throw new ArticleNotFoundException().addDetail("articleId", articleId);
		}

		return article;
	}

	private User validateUser(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException().addDetail("userId", userId));
	}

	private List<Article> readBackup(InputStream stream, Set<String> urls) throws IOException {
		try (BufferedReader reader =
				 new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			List<Article> restored = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				ArticleBackupDto backup = objectMapper.readValue(line, ArticleBackupDto.class);
				if (urls.contains(backup.sourceUrl())) {
					continue;
				}
				Article article = Article.builder()
					.id(backup.id())
					.source(ArticleSource.valueOf(backup.source()))
					.sourceUrl(backup.sourceUrl())
					.title(backup.title())
					.publishDate(backup.publishDate())
					.collectedAt(backup.collectedAt())
					.summary(backup.summary())
					.commentCount(backup.commentCount())
					.viewCount(backup.viewCount())
					.deletedAt(backup.deletedAt())
					.build();
				restored.add(article);
				urls.add(backup.sourceUrl());
			}
			return restored;
		}
	}
}
