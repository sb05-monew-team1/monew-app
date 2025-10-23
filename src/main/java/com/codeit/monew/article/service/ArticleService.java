package com.codeit.monew.article.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.article.dto.ArticleDto;
import com.codeit.monew.article.dto.ArticleSearchRequest;
import com.codeit.monew.article.repository.ArticleRepository;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final PageResponseMapper pageResponseMapper;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public CursorPageResponse<ArticleDto> search(ArticleSearchRequest request) {
		// User 도메인과 관련 커스텀 예외가 작성되면 마무리
		// if (!userRepository.existsById(request.monewRequestUserId())) {
		// 	throw new NoSuchElementException();
		// }

		Slice<ArticleDto> slice = articleRepository.search(request);
		String nextCursor = null;
		String nextAfter = null;

		if (slice.hasNext() && slice.getNumberOfElements() > 0) {
			ArticleDto last = slice.getContent().get(slice.getNumberOfElements() - 1);

			nextAfter = last.publishDate().toString();
			nextCursor = switch (request.orderBy()) {
				case "commentCount" -> String.valueOf(last.commentCount());
				case "viewCount" -> String.valueOf(last.viewCount());
				default -> nextAfter;
			};
		}

		return pageResponseMapper.toCursorPageResponse(slice, nextCursor, nextAfter);
	}
}
