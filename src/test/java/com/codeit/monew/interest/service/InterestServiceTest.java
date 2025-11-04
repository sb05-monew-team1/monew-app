package com.codeit.monew.interest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import com.codeit.monew.activity.service.UserActivityService;
import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.common.util.PageResponseMapper;
import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
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
import com.querydsl.core.types.Predicate;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

	@Mock
	private InterestRepository interestRepository;

	@Mock
	private InterestSubscriptionRepository interestSubscriptionRepository;

	@Mock
	private InterestMapper interestMapper;

	@Mock
	private PageResponseMapper pageResponseMapper;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserActivityService userActivityService;

	@InjectMocks
	private InterestService interestService;

	@Nested
	@DisplayName("관심사 등록 테스트")
	class CreateInterestTest {

		@Test
		@DisplayName("관심사 등록 성공")
		void createInterest_Success() {
			// given
			List<String> keywords = Arrays.asList("키워드1", "키워드2");
			InterestRegisterRequest request = new InterestRegisterRequest("관심사명", keywords);

			when(interestRepository.findAllNames()).thenReturn(List.of());

			Interest interest = Interest.builder()
				.name(request.name())
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();
			when(interestRepository.save(any(Interest.class))).thenReturn(interest);

			InterestDto expectedDto = new InterestDto(
				UUID.randomUUID(),
				"관심사명",
				keywords,
				0L,
				null
			);
			when(interestMapper.toDto(any(Interest.class), any())).thenReturn(expectedDto);

			// when
			InterestDto result = interestService.createInterest(request);

			// then
			assertNotNull(result);
			assertEquals(expectedDto.name(), result.name());
			verify(interestRepository, times(1)).save(any(Interest.class));
			verify(interestMapper, times(1)).toDto(any(Interest.class), isNull());
		}

		@Test
		@DisplayName("유사한 관심사명이 존재할 경우 예외 발생")
		void createInterest_Failure_SimilarNameExists() {
			// given
			InterestRegisterRequest request = new InterestRegisterRequest(
				"축구",
				Arrays.asList("키워드1")
			);

			when(interestRepository.findAllNames()).thenReturn(Arrays.asList("축구"));

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.createInterest(request);
			});

			assertEquals(ErrorCode.SIMILAR_INTEREST_EXISTS, exception.getErrorCode());
			verify(interestRepository, never()).save(any(Interest.class));
		}

		@Test
		@DisplayName("유사도가 임계값 미만일 경우 등록 성공")
		void createInterest_Success_BelowSimilarityThreshold() {
			// given
			InterestRegisterRequest request = new InterestRegisterRequest(
				"야구",
				Arrays.asList("키워드1")
			);

			when(interestRepository.findAllNames()).thenReturn(Arrays.asList("축구"));

			Interest interest = Interest.builder()
				.name(request.name())
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			when(interestRepository.save(any(Interest.class))).thenReturn(interest);

			InterestDto expectedDto = new InterestDto(
				UUID.randomUUID(),
				"야구",
				Arrays.asList("키워드1"),
				0L,
				null
			);
			when(interestMapper.toDto(any(Interest.class), any())).thenReturn(expectedDto);

			// when
			InterestDto result = interestService.createInterest(request);

			// then
			assertNotNull(result);
			verify(interestRepository, times(1)).save(any(Interest.class));
		}
	}

	@Nested
	@DisplayName("관심사 삭제 테스트")
	class DeleteInterestTest {

		@Test
		@DisplayName("관심사 물리 삭제 성공")
		void deleteInterest_Success() {
			// given
			UUID interestId = UUID.randomUUID();
			when(interestRepository.existsById(interestId)).thenReturn(true);

			// when
			interestService.deleteInterest(interestId);

			// then
			verify(interestRepository, times(1)).existsById(interestId);
			verify(interestRepository, times(1)).deleteById(interestId);
		}

		@Test
		@DisplayName("없는 관심사 ID 삭제 시 예외 발생")
		void deleteInterest_Failure_NotFound() {
			// given
			UUID nonExistentId = UUID.randomUUID();
			when(interestRepository.existsById(nonExistentId)).thenReturn(false);

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.deleteInterest(nonExistentId);
			});

			assertEquals(ErrorCode.INTEREST_NOT_FOUND, exception.getErrorCode());

			verify(interestRepository, never()).deleteById(nonExistentId);
		}
	}

	@Nested
	@DisplayName("관심사 정보 수정 테스트")
	class UpdateInterestTest {

		@Test
		@DisplayName("관심사 키워드 수정 성공")
		void updateInterest_Success() {
			// given
			UUID interestId = UUID.randomUUID();
			List<String> newKeywords = Arrays.asList("새키워드1", "새키워드2");
			InterestUpdateRequest request = new InterestUpdateRequest(newKeywords);

			Interest interest = Interest.builder()
				.name("관심사명")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			InterestKeyword oldKeyword = InterestKeyword.builder()
				.keyword("기존키워드")
				.interest(interest)
				.build();
			interest.getKeywords().add(oldKeyword);

			when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

			InterestDto expectedDto = new InterestDto(
				interestId,
				"관심사명",
				newKeywords,
				0L,
				null
			);
			when(interestMapper.toDto(any(Interest.class), any())).thenReturn(expectedDto);

			// when
			InterestDto result = interestService.updateInterest(interestId, request);

			// then
			assertNotNull(result);
			assertEquals(expectedDto.keywords(), result.keywords());
			verify(interestRepository, times(1)).flush();
			verify(interestMapper, times(1)).toDto(any(Interest.class), isNull());
		}

		@Test
		@DisplayName("없는 관심사 ID 수정 시 예외 발생")
		void updateInterest_Failure_NotFound() {
			// given
			UUID nonExistentId = UUID.randomUUID();
			InterestUpdateRequest request = new InterestUpdateRequest(Arrays.asList("키워드"));

			when(interestRepository.findById(nonExistentId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.updateInterest(nonExistentId, request);
			});

			assertEquals(ErrorCode.INTEREST_NOT_FOUND, exception.getErrorCode());
			verify(interestRepository, never()).flush();
		}

		@Test
		@DisplayName("기존 키워드와 새 키워드 혼합 수정 성공")
		void updateInterest_Success_MixedKeywords() {
			// given
			UUID interestId = UUID.randomUUID();
			List<String> newKeywords = Arrays.asList("기존키워드1", "새키워드1");
			InterestUpdateRequest request = new InterestUpdateRequest(newKeywords);

			Interest interest = Interest.builder()
				.name("관심사명")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			InterestKeyword existingKeyword1 = InterestKeyword.builder()
				.keyword("기존키워드1")
				.interest(interest)
				.build();
			InterestKeyword existingKeyword2 = InterestKeyword.builder()
				.keyword("기존키워드2")
				.interest(interest)
				.build();
			interest.getKeywords().add(existingKeyword1);
			interest.getKeywords().add(existingKeyword2);

			when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

			InterestDto expectedDto = new InterestDto(
				interestId,
				"관심사명",
				newKeywords,
				0L,
				null
			);
			when(interestMapper.toDto(any(Interest.class), any())).thenReturn(expectedDto);

			// when
			InterestDto result = interestService.updateInterest(interestId, request);

			// then
			assertNotNull(result);
			verify(interestRepository, times(1)).flush();
		}
	}

	@Nested
	@DisplayName("관심사 목록 조회 테스트")
	class GetInterestsTest {

		@Test
		@DisplayName("관심사 목록 조회 성공 - 키워드 없이")
		void getInterests_Success_NoKeyword() {
			// given
			UUID userId = UUID.randomUUID();
			InterestSearchRequest request = new InterestSearchRequest(
				null,
				"name",
				"ASC",
				null,
				null,
				10
			);

			Interest interest1 = Interest.builder()
				.name("관심사1")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();
			Interest interest2 = Interest.builder()
				.name("관심사2")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();
			List<Interest> interests = Arrays.asList(interest1, interest2);
			Pageable pageable = PageRequest.of(0, 11, Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.DESC, "createdAt")));
			Page<Interest> interestPage = new PageImpl<>(interests, pageable, interests.size());

			when(interestRepository.count(any(Predicate.class))).thenReturn(2L);
			when(interestRepository.findAll(any(Predicate.class), any(Pageable.class)))
				.thenReturn(interestPage);
			when(interestSubscriptionRepository.findInterestIdsByUserIdAndInterestIdsIn(any(), any()))
				.thenReturn(new HashSet<>());

			CursorPageResponse<InterestDto> expectedResponse = CursorPageResponse.<InterestDto>builder()
				.content(Arrays.asList(
					new InterestDto(UUID.randomUUID(), "관심사1", List.of(), 0L, false),
					new InterestDto(UUID.randomUUID(), "관심사2", List.of(), 0L, false)
				))
				.nextCursor(null)
				.nextAfter(null)
				.size(10)
				.totalElements(2L)
				.hasNext(false)
				.build();
			when(pageResponseMapper.toCursorPageResponse(any(Slice.class), any(), any(), anyLong()))
				.thenReturn(expectedResponse);

			// when
			CursorPageResponse<InterestDto> result = interestService.getInterests(request, userId);

			// then
			assertNotNull(result);
			assertEquals(2, result.content().size());
			assertEquals(2L, result.totalElements());
			assertFalse(result.hasNext());
			verify(interestRepository, times(1)).count(any(Predicate.class));
			verify(interestRepository, times(1)).findAll(any(Predicate.class), any(Pageable.class));
			verify(interestSubscriptionRepository, times(1))
				.findInterestIdsByUserIdAndInterestIdsIn(eq(userId), any());
		}

		@Test
		@DisplayName("관심사 목록 조회 성공 - 키워드 검색")
		void getInterests_Success_WithKeyword() {
			// given
			UUID userId = UUID.randomUUID();
			InterestSearchRequest request = new InterestSearchRequest("검색어", "name", "ASC", null, null, 10);

			Interest interest = Interest.builder()
				.name("검색어 포함 관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			List<Interest> interests = List.of(interest);
			Pageable pageable = PageRequest.of(0, 11, Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.DESC, "createdAt")));
			Page<Interest> interestPage = new PageImpl<>(interests, pageable, interests.size());

			when(interestRepository.count(any(Predicate.class))).thenReturn(1L);
			when(interestRepository.findAll(any(Predicate.class), any(Pageable.class)))
				.thenReturn(interestPage);
			when(interestSubscriptionRepository.findInterestIdsByUserIdAndInterestIdsIn(any(), any()))
				.thenReturn(new HashSet<>());

			CursorPageResponse<InterestDto> expectedResponse = CursorPageResponse.<InterestDto>builder()
				.content(List.of(
					new InterestDto(UUID.randomUUID(), "검색어 포함 관심사", List.of(), 0L, false)
				))
				.nextCursor(null)
				.nextAfter(null)
				.size(10)
				.totalElements(1L)
				.hasNext(false)
				.build();
			when(pageResponseMapper.toCursorPageResponse(any(Slice.class), any(), any(), anyLong()))
				.thenReturn(expectedResponse);

			// when
			CursorPageResponse<InterestDto> result = interestService.getInterests(request, userId);

			// then
			assertNotNull(result);
			assertEquals(1, result.content().size());
			assertEquals(1L, result.totalElements());
			verify(interestRepository, times(1)).count(any(Predicate.class));
			verify(interestRepository, times(1)).findAll(any(Predicate.class), any(Pageable.class));
		}

		@Test
		@DisplayName("관심사 목록 조회 - 빈 결과")
		void getInterests_EmptyResult() {
			// given
			UUID userId = UUID.randomUUID();
			InterestSearchRequest request = new InterestSearchRequest(null, "name", "ASC", null, null, 10);

			Page<Interest> emptyPage = new PageImpl<>(
				List.of(),
				PageRequest.of(0, 11, Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.DESC, "createdAt"))),
				0
			);

			when(interestRepository.count(any(Predicate.class))).thenReturn(0L);
			when(interestRepository.findAll(any(Predicate.class), any(Pageable.class)))
				.thenReturn(emptyPage);

			CursorPageResponse<InterestDto> expectedResponse = CursorPageResponse.<InterestDto>builder()
				.content(List.of())
				.nextCursor(null)
				.nextAfter(null)
				.size(10)
				.totalElements(0L)
				.hasNext(false)
				.build();
			when(pageResponseMapper.toCursorPageResponse(any(Slice.class), any(), any(), anyLong()))
				.thenReturn(expectedResponse);

			// when
			CursorPageResponse<InterestDto> result = interestService.getInterests(request, userId);

			// then
			assertNotNull(result);
			assertTrue(result.content().isEmpty());
			assertEquals(0L, result.totalElements());
			assertFalse(result.hasNext());
			verify(interestRepository, times(1)).count(any(Predicate.class));
		}
	}

	@Nested
	@DisplayName("관심사 구독 테스트")
	class CreateSubscriptionTest {

		@Test
		@DisplayName("관심사 구독 성공")
		void createSubscription_Success() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
			when(interestSubscriptionRepository.existsByUserAndInterest(user, interest))
				.thenReturn(false);

			InterestSubscription subscription = InterestSubscription.builder()
				.user(user)
				.interest(interest)
				.build();

			when(interestSubscriptionRepository.save(any(InterestSubscription.class)))
				.thenReturn(subscription);

			SubscriptionDto expectedDto = new SubscriptionDto(
				UUID.randomUUID(),
				interestId,
				"관심사",
				List.of(),
				1L,
				Instant.now()
			);
			when(interestMapper.toDto(any(InterestSubscription.class))).thenReturn(expectedDto);

			// when
			SubscriptionDto result = interestService.createSubscription(interestId, userId);

			// then
			assertNotNull(result);
			assertEquals(expectedDto.interestName(), result.interestName());
			verify(interestSubscriptionRepository, times(1)).save(any(InterestSubscription.class));
		}

		@Test
		@DisplayName("없는 사용자 ID로 구독 시 예외 발생")
		void createSubscription_Failure_UserNotFound() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.createSubscription(interestId, userId);
			});

			assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
			verify(interestSubscriptionRepository, never()).save(any());
		}

		@Test
		@DisplayName("없는 관심사 ID로 구독 시 예외 발생")
		void createSubscription_Failure_InterestNotFound() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			User user = User.builder()
				.email("test@test.com")
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.createSubscription(interestId, userId);
			});

			assertEquals(ErrorCode.INTEREST_NOT_FOUND, exception.getErrorCode());
			verify(interestSubscriptionRepository, never()).save(any());
		}

		@Test
		@DisplayName("이미 구독한 관심사 재구독 시 예외 발생")
		void createSubscription_Failure_AlreadySubscribed() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(1L)
				.keywords(new ArrayList<>())
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
			when(interestSubscriptionRepository.existsByUserAndInterest(user, interest))
				.thenReturn(true);

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.createSubscription(interestId, userId);
			});

			assertEquals(ErrorCode.ALREADY_SUBSCRIBED, exception.getErrorCode());
			verify(interestSubscriptionRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("관심사 구독 취소 테스트")
	class DeleteSubscriptionTest {

		@Test
		@DisplayName("관심사 구독 취소 성공")
		void deleteSubscription_Success() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(1L)
				.keywords(new ArrayList<>())
				.build();

			InterestSubscription subscription = InterestSubscription.builder()
				.user(user)
				.interest(interest)
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
			when(interestSubscriptionRepository.findByUserAndInterest(user, interest))
				.thenReturn(Optional.of(subscription));

			// when
			interestService.deleteSubscription(interestId, userId);

			// then
			verify(interestSubscriptionRepository, times(1)).delete(subscription);
		}

		@Test
		@DisplayName("없는 사용자 ID로 구독 취소 시 예외 발생")
		void deleteSubscription_Failure_UserNotFound() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.deleteSubscription(interestId, userId);
			});

			assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
			verify(interestSubscriptionRepository, never()).delete(any());
		}

		@Test
		@DisplayName("없는 관심사 ID로 구독 취소 시 예외 발생")
		void deleteSubscription_Failure_InterestNotFound() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			User user = User.builder()
				.email("test@test.com")
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.deleteSubscription(interestId, userId);
			});

			assertEquals(ErrorCode.INTEREST_NOT_FOUND, exception.getErrorCode());
			verify(interestSubscriptionRepository, never()).delete(any());
		}

		@Test
		@DisplayName("구독하지 않은 관심사 구독 취소 시 예외 발생")
		void deleteSubscription_Failure_SubscriptionNotFound() {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();

			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			when(userRepository.findById(userId)).thenReturn(Optional.of(user));
			when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
			when(interestSubscriptionRepository.findByUserAndInterest(user, interest))
				.thenReturn(Optional.empty());

			// when & then
			BusinessException exception = assertThrows(BusinessException.class, () -> {
				interestService.deleteSubscription(interestId, userId);
			});

			assertEquals(ErrorCode.SUBSCRIPTION_NOT_FOUND, exception.getErrorCode());
			verify(interestSubscriptionRepository, never()).delete(any());
		}
	}
}
