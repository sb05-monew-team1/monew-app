package com.codeit.monew.interest.service;

import static com.codeit.monew.interest.domain.QInterest.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
import com.codeit.monew.interest.domain.InterestOrder;
import com.codeit.monew.interest.domain.InterestSubscription;
import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.dto.InterestSearchRequest;
import com.codeit.monew.interest.dto.InterestUpdateRequest;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.mapper.InterestMapper;
import com.codeit.monew.interest.repository.InterestRepository;
import com.codeit.monew.interest.repository.InterestSubscriptionRepository;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestService {

	private final InterestRepository interestRepository;
	private final InterestSubscriptionRepository interestSubscriptionRepository;
	private final InterestMapper interestMapper;
	private final PageResponseMapper pageResponseMapper;
	private final UserRepository userRepository;

	private static final double SIMILARITY_THRESHOLD = 0.8;

	/**
	 * 관심사 등록
	 */
	@Transactional
	public InterestDto createInterest(InterestRegisterRequest request) {
		validateSimilarName(request.name());

		Interest interest = Interest.builder()
			.name(request.name())
			.subscriberCount(0L)
			.keywords(new ArrayList<>())
			.build();

		request.keywords().forEach(keywordString -> {
			InterestKeyword interestKeyword = InterestKeyword.builder()
				.keyword(keywordString)
				.interest(interest)
				.build();
			interest.getKeywords().add(interestKeyword);
		});

		Interest savedInterest = interestRepository.save(interest);

		return interestMapper.toDto(savedInterest, false);
	}

	/**
	 * 관심사 물리 삭제
	 */
	@Transactional
	public void deleteInterest(UUID interestId) {
		if (!interestRepository.existsById(interestId)) {
			throw new NoSuchElementException("해당 id의 관심사를 찾을 수 없습니다");
		}
		interestRepository.deleteById(interestId);

	}

	/**
	 * 관심사 정보 수정
	 */
	@Transactional
	public InterestDto updateInterest(UUID interestId, InterestUpdateRequest request) {
		Interest interest = interestRepository.findById(interestId).orElseThrow(
			() -> new NoSuchElementException("해당 id의 관심사를 찾을 수 없습니다.")
		);

		Set<String> existingKeywordString = interest.getKeywords().stream()
			.map(InterestKeyword::getKeyword)
			.collect(Collectors.toSet());

		Set<String> requestedKeywordString = new HashSet<>(request.keywords());

		interest.getKeywords().removeIf(
			keyword -> !requestedKeywordString.contains(keyword.getKeyword()));
		interestRepository.flush();

		requestedKeywordString.forEach(keywordString -> {
			if (!existingKeywordString.contains(keywordString)) {
				InterestKeyword newKeyword = InterestKeyword.builder()
					.keyword(keywordString)
					.interest(interest)
					.build();
				interest.getKeywords().add(newKeyword);
			}
		});

		return interestMapper.toDto(interest, false);
	}

	/**
	 * 관심사 목록 조회
	 */
	@Transactional
	public CursorPageResponse<InterestDto> getInterests(InterestSearchRequest request, UUID userId) {
		Sort.Direction sortDirection =
			"DESC".equalsIgnoreCase(request.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Sort primarySort = Sort.by(sortDirection, request.orderBy().name());
		Sort secondarySort = Sort.by(sortDirection, "createdAt");
		Pageable pageable = PageRequest.of(0, request.limit(), primarySort.and(secondarySort));

		Predicate dataPredicate = buildPredicate(request.keyword(), request.orderBy(), request.cursor(),
			request.after(), sortDirection);
		Predicate countPredicate = buildCountPredicate(request.keyword());

		Slice<Interest> interestSlice;
		long totalElements;

		if (dataPredicate == null) {
			interestSlice = interestRepository.findAll(pageable);
			totalElements = interestRepository.count();
		} else {
			interestSlice = interestRepository.findAll(dataPredicate, pageable);
			totalElements = (countPredicate == null)
				? interestRepository.count()
				: interestRepository.count(countPredicate);
		}

		if (interestSlice.getContent().isEmpty()) {
			return pageResponseMapper.toCursorPageResponse(
				new SliceImpl<>(List.of(), interestSlice.getPageable(), false),
				null,
				null,
				totalElements);
		}

		List<UUID> interestIds = interestSlice.getContent().stream().map(Interest::getId).toList();
		Set<UUID> subscribedInterestIds = interestSubscriptionRepository
			.findInterestIdsByUserIdAndInterestIdsIn(userId, interestIds);
		List<InterestDto> dtoList = interestMapper.toDtoList(interestSlice.getContent(), subscribedInterestIds);

		String nextCursor = null;
		Instant nextAfter = null;
		if (interestSlice.hasNext()) {
			Interest lastInterest = interestSlice.getContent().get(interestSlice.getContent().size() - 1);
			nextCursor = (request.orderBy() == InterestOrder.name) ?
				lastInterest.getName() : String.valueOf(lastInterest.getSubscriberCount());
			nextAfter = lastInterest.getCreatedAt();
		}

		return pageResponseMapper.toCursorPageResponse(
			new SliceImpl<>(dtoList, interestSlice.getPageable(), interestSlice.hasNext()),
			nextCursor,
			nextAfter,
			totalElements);
	}

	/**
	 * 관심사 구독
	 */
	@Transactional
	public SubscriptionDto createSubscription(UUID interestId, UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

		Interest interest = interestRepository.findById(interestId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

		if (interestSubscriptionRepository.existsByUserAndInterest(user, interest)) {
			throw new BusinessException(ErrorCode.ALREADY_SUBSCRIBED);
		}

		InterestSubscription newSubscription = InterestSubscription.builder()
			.user(user)
			.interest(interest)
			.build();

		interest.increaseSubscriberCount();

		InterestSubscription savedSubscription = interestSubscriptionRepository.save(newSubscription);

		return interestMapper.toDto(savedSubscription);
	}

	/**
	 * Querydsl Predicate를 생성하는 메서드
	 */
	private Predicate buildPredicate(String keyword, InterestOrder orderBy, String cursor, Instant after,
		Sort.Direction direction) {
		BooleanBuilder builder = new BooleanBuilder();

		if (StringUtils.hasText(keyword)) {
			builder.and(interest.name.containsIgnoreCase(keyword)
				.or(interest.keywords.any().keyword.containsIgnoreCase(keyword)));
		}

		if (StringUtils.hasText(cursor) && after != null) {
			boolean isAsc = direction == Sort.Direction.ASC;

			if (orderBy == InterestOrder.name) {
				if (isAsc) {
					builder.and(
						interest.name.gt(cursor)
							.or(interest.name.eq(cursor).and(interest.createdAt.gt(after))));
				} else {
					builder.and(
						interest.name.lt(cursor)
							.or(interest.name.eq(cursor).and(interest.createdAt.lt(after))));
				}
			} else { // subscriberCount
				long subscriberCount = Long.parseLong(cursor);
				if (isAsc) {
					builder.and(interest.subscriberCount.gt(subscriberCount)
						.or(interest.subscriberCount.eq(subscriberCount).and(interest.createdAt.gt(after))));
				} else {
					builder.and(interest.subscriberCount.lt(subscriberCount)
						.or(interest.subscriberCount.eq(subscriberCount).and(interest.createdAt.lt(after))));
				}
			}
		}

		return builder.getValue();
	}

	/**
	 * 전체 개수 조회를 위한 Predicate (커서 조건 제외)
	 */
	private Predicate buildCountPredicate(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(interest.name.containsIgnoreCase(keyword)
			.or(interest.keywords.any().keyword.containsIgnoreCase(keyword)));

		return builder.getValue();
	}

	/**
	 * 관심사 유사도 검증 메서드
	 */
	private void validateSimilarName(String newName) {
		List<String> existingNames = interestRepository.findAllNames();
		JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

		for (String existingName : existingNames) {
			double score = similarity.apply(
				newName.toLowerCase().trim(),
				existingName.toLowerCase().trim()
			);

			if (score >= SIMILARITY_THRESHOLD) {
				throw new BusinessException(ErrorCode.SIMILAR_INTEREST_EXISTS);
			}
		}
	}
}