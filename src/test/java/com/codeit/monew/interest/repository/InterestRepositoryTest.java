package com.codeit.monew.interest.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.codeit.monew.common.config.QuerydslConfig;
import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
import com.codeit.monew.interest.domain.QInterest;
import com.querydsl.core.BooleanBuilder;

@DataJpaTest
@Import(QuerydslConfig.class)
class InterestRepositoryTest {

	@Autowired
	private InterestRepository interestRepository;

	@Autowired
	private TestEntityManager entityManager;

	@BeforeEach
	void setUp() {
		interestRepository.deleteAll();
	}

	@Nested
	@DisplayName("findAllNames 테스트")
	class FindAllNamesTest {

		@Test
		@DisplayName("모든 관심사 이름 조회 성공")
		void findAllNames_Success() {
			// given
			Interest interest1 = Interest.builder().name("관심사1").subscriberCount(0L).build();
			Interest interest2 = Interest.builder().name("관심사2").subscriberCount(0L).build();
			interestRepository.saveAll(Arrays.asList(interest1, interest2));
			entityManager.flush();
			entityManager.clear();

			// when
			List<String> names = interestRepository.findAllNames();

			// then
			assertEquals(2, names.size());
			assertTrue(names.contains("관심사1"));
			assertTrue(names.contains("관심사2"));
		}
	}

	@Nested
	@DisplayName("QueryDSL Predicate 테스트")
	class QueryDslPredicateTest {

		@Test
		@DisplayName("이름으로 검색 성공")
		void findByNameContaining_Success() {
			// given
			Interest interest1 = Interest.builder().name("축구").subscriberCount(0L).build();
			Interest interest2 = Interest.builder().name("야구").subscriberCount(0L).build();
			interestRepository.saveAll(Arrays.asList(interest1, interest2));
			entityManager.flush();

			BooleanBuilder builder = new BooleanBuilder();
			builder.and(QInterest.interest.name.containsIgnoreCase("축구"));

			// when
			List<Interest> resultList = (List<Interest>)interestRepository.findAll(builder);

			// then
			assertEquals(1, resultList.size());
			assertEquals("축구", resultList.get(0).getName());
		}

		@Test
		@DisplayName("키워드로 검색 성공")
		void findByKeywordContaining_Success() {
			// given
			Interest interest = Interest.builder().name("스포츠").subscriberCount(0L).build();
			InterestKeyword keyword = InterestKeyword.builder().keyword("축구").interest(interest).build();
			interest.getKeywords().add(keyword);
			interestRepository.save(interest);
			entityManager.flush();

			BooleanBuilder builder = new BooleanBuilder();
			builder.and(QInterest.interest.keywords.any().keyword.containsIgnoreCase("축구"));

			// when
			List<Interest> resultList = (List<Interest>)interestRepository.findAll(builder);

			// then
			assertEquals(1, resultList.size());
			assertEquals("스포츠", resultList.get(0).getName());
		}
	}

	@Nested
	@DisplayName("관심사 저장 및 조회 테스트")
	class SaveAndFindTest {

		@Test
		@DisplayName("관심사와 키워드 함께 저장 성공 (Cascade)")
		void saveInterestWithKeywords_Success() {
			// given
			Interest interest = Interest.builder().name("IT").subscriberCount(10L).build();
			InterestKeyword keyword = InterestKeyword.builder().keyword("Java").interest(interest).build();
			interest.getKeywords().add(keyword);

			// when
			Interest savedInterest = interestRepository.save(interest);
			entityManager.flush();
			entityManager.clear();

			Interest foundInterest = interestRepository.findById(savedInterest.getId()).orElseThrow();

			// then
			assertNotNull(foundInterest);
			assertEquals("IT", foundInterest.getName());
			assertEquals(1, foundInterest.getKeywords().size());
			assertEquals("Java", foundInterest.getKeywords().get(0).getKeyword());
		}
	}
}