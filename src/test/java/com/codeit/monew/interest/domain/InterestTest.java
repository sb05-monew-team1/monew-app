package com.codeit.monew.interest.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InterestTest {

	@Nested
	@DisplayName("구독자 수 증가 테스트")
	class IncreaseSubscriberCountTest {

		@Test
		@DisplayName("구독자 수 증가 성공")
		void increaseSubscriberCount_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.build();

			// when
			interest.increaseSubscriberCount();

			// then
			assertEquals(1L, interest.getSubscriberCount());
		}

		@Test
		@DisplayName("여러 번 증가 성공")
		void increaseSubscriberCount_Multiple() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(5L)
				.build();

			// when
			interest.increaseSubscriberCount();
			interest.increaseSubscriberCount();
			interest.increaseSubscriberCount();

			// then
			assertEquals(8L, interest.getSubscriberCount());
		}
	}

	@Nested
	@DisplayName("구독자 수 감소 테스트")
	class DecreaseSubscriberCountTest {

		@Test
		@DisplayName("구독자 수 감소 성공")
		void decreaseSubscriberCount_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(5L)
				.build();

			// when
			interest.decreaseSubscriberCount();

			// then
			assertEquals(4L, interest.getSubscriberCount());
		}

		@Test
		@DisplayName("구독자 수가 0일 때 감소하지 않음")
		void decreaseSubscriberCount_NotBelowZero() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.build();

			// when
			interest.decreaseSubscriberCount();

			// then
			assertEquals(0L, interest.getSubscriberCount());
		}

		@Test
		@DisplayName("구독자 수가 1일 때 0으로 감소")
		void decreaseSubscriberCount_ToZero() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(1L)
				.build();

			// when
			interest.decreaseSubscriberCount();

			// then
			assertEquals(0L, interest.getSubscriberCount());
		}
	}

	@Nested
	@DisplayName("키워드 관리 테스트")
	class KeywordManagementTest {

		@Test
		@DisplayName("키워드 추가 성공")
		void addKeyword_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.build();

			InterestKeyword keyword = InterestKeyword.builder()
				.keyword("키워드1")
				.interest(interest)
				.build();

			// when
			interest.getKeywords().add(keyword);

			// then
			assertEquals(1, interest.getKeywords().size());
			assertEquals("키워드1", interest.getKeywords().get(0).getKeyword());
		}

		@Test
		@DisplayName("여러 키워드 추가 성공")
		void addMultipleKeywords_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.build();

			InterestKeyword keyword1 = InterestKeyword.builder()
				.keyword("키워드1")
				.interest(interest)
				.build();

			InterestKeyword keyword2 = InterestKeyword.builder()
				.keyword("키워드2")
				.interest(interest)
				.build();

			// when
			interest.getKeywords().add(keyword1);
			interest.getKeywords().add(keyword2);

			// then
			assertEquals(2, interest.getKeywords().size());
		}

		@Test
		@DisplayName("키워드 제거 성공")
		void removeKeyword_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.build();

			InterestKeyword keyword1 = InterestKeyword.builder()
				.keyword("키워드1")
				.interest(interest)
				.build();

			InterestKeyword keyword2 = InterestKeyword.builder()
				.keyword("키워드2")
				.interest(interest)
				.build();

			interest.getKeywords().add(keyword1);
			interest.getKeywords().add(keyword2);

			// when
			interest.getKeywords().removeIf(k -> k.getKeyword().equals("키워드1"));

			// then
			assertEquals(1, interest.getKeywords().size());
			assertEquals("키워드2", interest.getKeywords().get(0).getKeyword());
		}
	}

	@Nested
	@DisplayName("빌더 패턴 테스트")
	class BuilderTest {

		@Test
		@DisplayName("기본 빌더로 생성 성공")
		void builder_Success() {
			// when
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.build();

			// then
			assertNotNull(interest);
			assertEquals("관심사", interest.getName());
			assertEquals(0L, interest.getSubscriberCount());
			assertNotNull(interest.getKeywords());
		}

		@Test
		@DisplayName("키워드와 함께 생성 성공")
		void builderWithKeywords_Success() {
			// given
			List<InterestKeyword> keywords = new ArrayList<>();

			// when
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(keywords)
				.build();

			InterestKeyword keyword = InterestKeyword.builder()
				.keyword("키워드1")
				.interest(interest)
				.build();

			interest.getKeywords().add(keyword);

			// then
			assertNotNull(interest);
			assertEquals(1, interest.getKeywords().size());
		}
	}
}