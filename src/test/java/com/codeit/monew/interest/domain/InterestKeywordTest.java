package com.codeit.monew.interest.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InterestKeywordTest {

	@Nested
	@DisplayName("InterestKeyword 생성 테스트")
	class CreationTest {

		@Test
		@DisplayName("키워드 생성 성공")
		void createKeyword_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			// when
			InterestKeyword keyword = InterestKeyword.builder()
				.keyword("키워드")
				.interest(interest)
				.build();

			// then
			assertNotNull(keyword);
			assertEquals("키워드", keyword.getKeyword());
			assertEquals(interest, keyword.getInterest());
		}

		@Test
		@DisplayName("여러 키워드 생성 성공")
		void createMultipleKeywords_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			// when
			InterestKeyword keyword1 = InterestKeyword.builder()
				.keyword("키워드1")
				.interest(interest)
				.build();

			InterestKeyword keyword2 = InterestKeyword.builder()
				.keyword("키워드2")
				.interest(interest)
				.build();

			// then
			assertNotNull(keyword1);
			assertNotNull(keyword2);
			assertEquals("키워드1", keyword1.getKeyword());
			assertEquals("키워드2", keyword2.getKeyword());
			assertEquals(interest, keyword1.getInterest());
			assertEquals(interest, keyword2.getInterest());
		}
	}

	@Nested
	@DisplayName("연관관계 테스트")
	class RelationshipTest {

		@Test
		@DisplayName("Interest와 양방향 연관관계 확인")
		void bidirectionalRelationship_Success() {
			// given
			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			InterestKeyword keyword = InterestKeyword.builder()
				.keyword("키워드")
				.interest(interest)
				.build();

			// when
			interest.getKeywords().add(keyword);

			// then
			assertEquals(1, interest.getKeywords().size());
			assertEquals(interest, keyword.getInterest());
			assertTrue(interest.getKeywords().contains(keyword));
		}
	}
}