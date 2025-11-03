package com.codeit.monew.interest.service;

import static com.codeit.monew.interest.domain.QInterest.*;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
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
import com.querydsl.core.types.dsl.ComparableExpressionBase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		log.debug("관심사 등록 시작 - name: {}", request.name());

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
		log.info("관심사 등록 - id: {}, name: {}, keywords: {}",
			savedInterest.getId(), savedInterest.getName(),
			savedInterest.getKeywords().stream()
				.map(InterestKeyword::getKeyword)
				.collect(Collectors.joining(", ")));

		return interestMapper.toDto(savedInterest, null);
	}

	/**
	 * 관심사 물리 삭제
	 */
	@Transactional
	public void deleteInterest(UUID interestId) {
		log.debug("요청 관심사 존재 확인 - interestId: {}", interestId);

		if (!interestRepository.existsById(interestId)) {
			throw new BusinessException(ErrorCode.INTEREST_NOT_FOUND);
		}
		interestRepository.deleteById(interestId);
		log.info("관심사 삭제 - interestId: {}", interestId);
	}

	/**
	 * 관심사 정보 수정
	 */
	@Transactional
	public InterestDto updateInterest(UUID interestId, InterestUpdateRequest request) {
		log.debug("관심사 정보 수정 시작 - interestId: {}", interestId);

		Interest interest = interestRepository.findById(interestId).orElseThrow(
			() -> new BusinessException(ErrorCode.INTEREST_NOT_FOUND)
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

		log.info("관심사 정보 수정 - id: {}, name: {}, keywords: {}", interest.getId(), interest.getName(),
			interest.getKeywords().stream()
				.map(InterestKeyword::getKeyword)
				.collect(Collectors.joining(", ")));

		return interestMapper.toDto(interest, null);
	}

	/**
	 * 관심사 목록 조회
	 */
	@Transactional
	public CursorPageResponse<InterestDto> getInterests(InterestSearchRequest request, UUID userId) {
		log.debug("관심사 목록 조회 시작 - cursor: [{}], after: [{}], orderBy: [{}], direction: [{}]",
			request.cursor(), request.after(), request.orderBy(), request.direction());

		String orderByString = request.orderBy().trim();
		String orderByField;

		if ("name".equalsIgnoreCase(orderByString)) {
			orderByField = "name";
		} else if ("subscriberCount".equalsIgnoreCase(orderByString)) {
			orderByField = "subscriberCount";
		} else {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		Sort.Direction direction = "ASC".equalsIgnoreCase(request.direction()) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Sort sort = Sort.by(direction, orderByField).and(Sort.by(Sort.Direction.DESC, "createdAt"));

		int pageSize = request.limit();
		Pageable pageable = PageRequest.of(0, pageSize + 1, sort);

		BooleanBuilder filterPredicate = new BooleanBuilder();
		if (StringUtils.hasText(request.keyword())) {
			String keyword = request.keyword();
			filterPredicate.and(interest.name.containsIgnoreCase(keyword)
				.or(interest.keywords.any().keyword.containsIgnoreCase(keyword)));
		}

		long totalElements = interestRepository.count(filterPredicate);

		BooleanBuilder finalPredicate = new BooleanBuilder().and(filterPredicate);

		if (StringUtils.hasText(request.cursor())) {
			BooleanBuilder cursorBuilder = new BooleanBuilder();

			if (StringUtils.hasText(request.after())) {
				log.debug("커서 로직 실행 (Standard) - cursor: [{}], after: [{}]", request.cursor(), request.after());
				Instant after;
				try {
					after = Instant.parse(request.after());
				} catch (DateTimeParseException e) {
					log.error("Instant 파싱 실패: {}", request.after(), e);
					throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
				}

				switch (orderByField) {
					case "name": {
						String cursor = request.cursor();
						if (direction == Sort.Direction.ASC) {
							cursorBuilder.or(interest.name.gt(cursor));
							cursorBuilder.or(interest.name.eq(cursor).and(interest.createdAt.lt(after)));
						} else {
							cursorBuilder.or(interest.name.lt(cursor));
							cursorBuilder.or(interest.name.eq(cursor).and(interest.createdAt.lt(after)));
						}
						break;
					}
					case "subscriberCount": {
						try {
							Long cursorSubscriberCount = Long.parseLong(request.cursor());
							if (direction == Sort.Direction.ASC) {
								cursorBuilder.or(interest.subscriberCount.gt(cursorSubscriberCount));
								cursorBuilder.or(interest.subscriberCount.eq(cursorSubscriberCount).and(interest.createdAt.lt(after)));
							} else {
								cursorBuilder.or(interest.subscriberCount.lt(cursorSubscriberCount));
								cursorBuilder.or(interest.subscriberCount.eq(cursorSubscriberCount).and(interest.createdAt.lt(after)));
							}
						} catch (NumberFormatException e) {
							log.error("Long 파싱 실패: {}", request.cursor(), e);
							throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
						}
						break;
					}
				}

			} else {
				log.warn("커서 로직 실행 (Fallback) - 'after' 파라미터가 누락되었습니다. 페이징 시 데이터가 누락될 수 있습니다.");

				switch (orderByField) {
					case "name": {
						String cursor = request.cursor();
						if (direction == Sort.Direction.ASC) {
							cursorBuilder.or(interest.name.gt(cursor));
						} else {
							cursorBuilder.or(interest.name.lt(cursor));
						}
						break;
					}
					case "subscriberCount": {
						try {
							Long cursorSubscriberCount = Long.parseLong(request.cursor());
							if (direction == Sort.Direction.ASC) {
								cursorBuilder.or(interest.subscriberCount.gt(cursorSubscriberCount));
							} else {
								cursorBuilder.or(interest.subscriberCount.lt(cursorSubscriberCount));
							}
						} catch (NumberFormatException e) {
							log.error("Long 파싱 실패 (Fallback): {}", request.cursor(), e);
							throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
						}
						break;
					}
				}
			}
			finalPredicate.and(cursorBuilder);

		} else {
			log.debug("커서 로직 건너뜀 (첫 페이지 요청) - cursor: [{}], after: [{}]", request.cursor(), request.after());
		}

		List<Interest> interests = interestRepository.findAll(finalPredicate, pageable).getContent();

		boolean hasNext = interests.size() > pageSize;
		List<Interest> content = hasNext ? interests.subList(0, pageSize) : interests;
		Slice<Interest> slice = new SliceImpl<>(content, PageRequest.of(0, pageSize), hasNext);

		List<InterestDto> dtos;
		if (content.isEmpty()) {
			dtos = new ArrayList<>();
		} else {
			List<UUID> interestIds = content.stream().map(Interest::getId).collect(Collectors.toList());
			Set<UUID> subscribedInterestIds = interestSubscriptionRepository
				.findInterestIdsByUserIdAndInterestIdsIn(userId, interestIds);

			dtos = content.stream()
				.map(i -> new InterestDto(
					i.getId(),
					i.getName(),
					i.getKeywords().stream().map(InterestKeyword::getKeyword).collect(Collectors.toList()),
					i.getSubscriberCount(),
					subscribedInterestIds.contains(i.getId())
				))
				.collect(Collectors.toList());
		}

		String nextCursor = null;
		String nextAfter = null;

		if (hasNext) {
			Interest lastInterest = content.get(content.size() - 1);
			Object cursorValue = switch (orderByField) {
				case "name" -> lastInterest.getName();
				case "subscriberCount" -> lastInterest.getSubscriberCount();
				default -> null;
			};
			nextCursor = String.valueOf(cursorValue);
			nextAfter = lastInterest.getCreatedAt().toString();

			log.debug("다음 페이지 커서 생성 - nextCursor: [{}], nextAfter: [{}]", nextCursor, nextAfter);
		}

		Slice<InterestDto> dtoSlice = new SliceImpl<>(dtos, slice.getPageable(), hasNext);
		return pageResponseMapper.toCursorPageResponse(dtoSlice, nextCursor, nextAfter, totalElements);
	}


	/**
	 * 관심사 구독
	 */
	@Transactional
	public SubscriptionDto createSubscription(UUID interestId, UUID userId) {
		log.debug("관심사 구독 시작 - userId: {}, interestId: {}", userId, interestId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				return new BusinessException(ErrorCode.USER_NOT_FOUND);
			});

		Interest interest = interestRepository.findById(interestId)
			.orElseThrow(() -> {
				return new BusinessException(ErrorCode.INTEREST_NOT_FOUND);
			});

		if (interestSubscriptionRepository.existsByUserAndInterest(user, interest)) {
			throw new BusinessException(ErrorCode.ALREADY_SUBSCRIBED);
		}

		InterestSubscription newSubscription = InterestSubscription.builder()
			.user(user)
			.interest(interest)
			.build();

		interest.increaseSubscriberCount();

		InterestSubscription savedSubscription = interestSubscriptionRepository.save(newSubscription);
		log.info("관심사 구독 - subscriptionId: {}, interestId: {}", savedSubscription.getId(), interestId);

		return interestMapper.toDto(savedSubscription);
	}

	/**
	 * 관심사 구독 취소
	 */
	@Transactional
	public void deleteSubscription(UUID interestId, UUID userId) {
		log.debug("관심사 구독 취소 시작 - userId: {}, interestId: {}", userId, interestId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		Interest interest = interestRepository.findById(interestId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INTEREST_NOT_FOUND));

		InterestSubscription subscription = interestSubscriptionRepository.findByUserAndInterest(user, interest)
			.orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

		interestSubscriptionRepository.delete(subscription);

		interest.decreaseSubscriberCount();
		log.info("관심사 구독 취소 - userId: {}, interestId: {}", userId, interestId);
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