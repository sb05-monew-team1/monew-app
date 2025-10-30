package com.codeit.monew.interest.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import com.codeit.monew.interest.domain.InterestSubscription;
import com.codeit.monew.user.domain.User;

@DataJpaTest
@Import(QuerydslConfig.class)
class InterestSubscriptionRepositoryTest {

	@Autowired
	private InterestSubscriptionRepository interestSubscriptionRepository;

	@Autowired
	private TestEntityManager entityManager;

	private User testUser;
	private Interest testInterest;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.email("test@test.com")
			.nickname("testuser")
			.password("password123")
			.build();
		entityManager.persist(testUser);

		testInterest = Interest.builder().name("테스트 관심사").subscriberCount(0L).build();
		entityManager.persist(testInterest);

		entityManager.flush();
		entityManager.clear();
	}

	@Nested
	@DisplayName("findInterestIdsByUserIdAndInterestIdsIn 테스트")
	class FindInterestIdsByUserIdTest {

		@Test
		@DisplayName("사용자가 구독한 관심사 ID 목록 조회 성공")
		void findInterestIdsByUserIdAndInterestIdsIn_Success() {
			// given
			Interest interest2 = Interest.builder().name("관심사2").subscriberCount(0L).build();
			entityManager.persist(interest2);

			InterestSubscription subscription = InterestSubscription.builder()
				.user(testUser)
				.interest(testInterest)
				.build();
			entityManager.persist(subscription);
			entityManager.flush();

			List<UUID> interestIds = Arrays.asList(testInterest.getId(), interest2.getId());

			// when
			Set<UUID> subscribedIds = interestSubscriptionRepository
				.findInterestIdsByUserIdAndInterestIdsIn(testUser.getId(), interestIds);

			// then
			assertEquals(1, subscribedIds.size());
			assertTrue(subscribedIds.contains(testInterest.getId()));
		}
	}

	@Nested
	@DisplayName("findByUserAndInterest 테스트")
	class FindByUserAndInterestTest {

		@Test
		@DisplayName("구독 정보 조회 성공")
		void findByUserAndInterest_Success() {
			// given
			InterestSubscription subscription = InterestSubscription.builder()
				.user(testUser)
				.interest(testInterest)
				.build();
			entityManager.persist(subscription);
			entityManager.flush();
			entityManager.clear();

			// when
			Optional<InterestSubscription> found = interestSubscriptionRepository
				.findByUserAndInterest(testUser, testInterest);

			// then
			assertTrue(found.isPresent());
			assertEquals(testUser.getId(), found.get().getUser().getId());
			assertEquals(testInterest.getId(), found.get().getInterest().getId());
		}
	}
}