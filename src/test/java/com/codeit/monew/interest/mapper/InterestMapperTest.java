package com.codeit.monew.interest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
import com.codeit.monew.interest.domain.InterestSubscription;
import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.user.domain.User;

class InterestMapperTest {

	private final InterestMapper mapper = Mappers.getMapper(InterestMapper.class);

	@Test
	void mapKeywordsReturnsKeywordList() {
		Interest interest = Interest.builder()
			.name("Tech")
			.subscriberCount(0L)
			.build();

		InterestKeyword keyword = InterestKeyword.builder()
			.interest(interest)
			.keyword("AI")
			.build();

		List<String> mapped = mapper.mapKeywords(List.of(keyword));

		assertThat(mapped).containsExactly("AI");
	}

	@Test
	void mapKeywordsHandlesNull() {
		assertThat(mapper.mapKeywords(null)).isNull();
	}

	@Test
	void toDtoReflectsSubscribedState() {
		Interest interest = Interest.builder()
			.name("Finance")
			.subscriberCount(2L)
			.build();
		ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

		InterestDto dto = mapper.toDto(interest, Boolean.TRUE);

		assertThat(dto.subscribedByMe()).isTrue();
		assertThat(dto.name()).isEqualTo("Finance");
	}

	@Test
	void toDtoListMarksSubscribedInterests() {
		Interest interest1 = Interest.builder()
			.name("Tech")
			.subscriberCount(10L)
			.build();
		Interest interest2 = Interest.builder()
			.name("Finance")
			.subscriberCount(5L)
			.build();

		UUID subscribedId = UUID.randomUUID();
		ReflectionTestUtils.setField(interest1, "id", subscribedId);
		ReflectionTestUtils.setField(interest2, "id", UUID.randomUUID());

		List<InterestDto> dtos = mapper.toDtoList(List.of(interest1, interest2), Set.of(subscribedId));

		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).subscribedByMe()).isTrue();
		assertThat(dtos.get(1).subscribedByMe()).isFalse();
	}

	@Test
	void subscriptionMappingIncludesNestedFields() {
		User user = User.register("user@example.com", "user", "encoded");

		Interest interest = Interest.builder()
			.name("Tech")
			.subscriberCount(3L)
			.build();
		ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

		InterestSubscription subscription = InterestSubscription.builder()
			.interest(interest)
			.user(user)
			.build();

		UUID subscriptionId = UUID.randomUUID();
		ReflectionTestUtils.setField(subscription, "id", subscriptionId);
		ReflectionTestUtils.setField(subscription, "createdAt", Instant.parse("2024-01-01T00:00:00Z"));

		SubscriptionDto dto = mapper.toDto(subscription);

		assertThat(dto.id()).isEqualTo(subscriptionId);
		assertThat(dto.interestId()).isEqualTo(interest.getId());
		assertThat(dto.interestName()).isEqualTo("Tech");
		assertThat(dto.interestSubscriberCount()).isEqualTo(3L);
		assertThat(dto.createdAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
	}
}
