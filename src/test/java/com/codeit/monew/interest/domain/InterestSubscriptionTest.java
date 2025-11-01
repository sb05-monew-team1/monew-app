package com.codeit.monew.interest.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.codeit.monew.user.domain.User;

class InterestSubscriptionTest {

	@Nested
	@DisplayName("InterestSubscription 생성 테스트")
	class CreationTest {

		@Test
		@DisplayName("구독 생성 성공")
		void createSubscription_Success() {
			// given
			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.build();

			// when
			InterestSubscription subscription = InterestSubscription.builder()
				.user(user)
				.interest(interest)
				.build();

			// then
			assertNotNull(subscription);
			assertEquals(user, subscription.getUser());
			assertEquals(interest, subscription.getInterest());
		}
	}

	@Nested
	@DisplayName("연관관계 테스트")
	class RelationshipTest {

		@Test
		@DisplayName("User와 Interest의 연관관계 확인")
		void relationships_Success() {
			// given
			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.subscriptions(new ArrayList<>())
				.build();

			// when
			InterestSubscription subscription = InterestSubscription.builder()
				.user(user)
				.interest(interest)
				.build();

			interest.getSubscriptions().add(subscription);

			// then
			assertEquals(user, subscription.getUser());
			assertEquals(interest, subscription.getInterest());
			assertEquals(1, interest.getSubscriptions().size());
			assertTrue(interest.getSubscriptions().contains(subscription));
		}

		@Test
		@DisplayName("한 사용자가 여러 관심사 구독")
		void multipleSubscriptionsPerUser_Success() {
			// given
			User user = User.builder()
				.email("test@test.com")
				.build();

			Interest interest1 = Interest.builder()
				.name("관심사1")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.subscriptions(new ArrayList<>())
				.build();

			Interest interest2 = Interest.builder()
				.name("관심사2")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.subscriptions(new ArrayList<>())
				.build();

			// when
			InterestSubscription subscription1 = InterestSubscription.builder()
				.user(user)
				.interest(interest1)
				.build();

			InterestSubscription subscription2 = InterestSubscription.builder()
				.user(user)
				.interest(interest2)
				.build();

			interest1.getSubscriptions().add(subscription1);
			interest2.getSubscriptions().add(subscription2);

			// then
			assertEquals(user, subscription1.getUser());
			assertEquals(user, subscription2.getUser());
			assertEquals(interest1, subscription1.getInterest());
			assertEquals(interest2, subscription2.getInterest());
		}

		@Test
		@DisplayName("한 관심사에 여러 사용자 구독")
		void multipleSubscribersPerInterest_Success() {
			// given
			User user1 = User.builder()
				.email("user1@test.com")
				.build();

			User user2 = User.builder()
				.email("user2@test.com")
				.build();

			Interest interest = Interest.builder()
				.name("관심사")
				.subscriberCount(0L)
				.keywords(new ArrayList<>())
				.subscriptions(new ArrayList<>())
				.build();

			// when
			InterestSubscription subscription1 = InterestSubscription.builder()
				.user(user1)
				.interest(interest)
				.build();

			InterestSubscription subscription2 = InterestSubscription.builder()
				.user(user2)
				.interest(interest)
				.build();

			interest.getSubscriptions().add(subscription1);
			interest.getSubscriptions().add(subscription2);

			// then
			assertEquals(2, interest.getSubscriptions().size());
			assertTrue(interest.getSubscriptions().contains(subscription1));
			assertTrue(interest.getSubscriptions().contains(subscription2));
		}
	}
}