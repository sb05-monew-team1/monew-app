package com.codeit.monew.interest.mapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
import com.codeit.monew.interest.domain.InterestSubscription;
import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.SubscriptionDto;

@Mapper(componentModel = "spring")
public interface InterestMapper {

	@Mapping(target = "keywords", source = "interest.keywords")
	@Mapping(target = "subscribedByMe", source = "subscribedByMe")
	InterestDto toDto(Interest interest, boolean subscribedByMe);

	@Mapping(source = "subscription.id", target = "id")
	@Mapping(source = "subscription.interest.id", target = "interestId")
	@Mapping(source = "subscription.interest.name", target = "interestName")
	@Mapping(source = "subscription.interest.keywords", target = "interestKeywords")
	@Mapping(source = "subscription.interest.subscriberCount", target = "interestSubscriberCount")
	@Mapping(source = "subscription.createdAt", target = "createdAt")
	SubscriptionDto toDto(InterestSubscription subscription);

	default List<String> mapKeywords(List<InterestKeyword> keywords) {
		if (keywords == null) {
			return null;
		}
		return keywords.stream()
			.map(InterestKeyword::getKeyword)
			.collect(Collectors.toList());
	}

	default List<InterestDto> toDtoList(List<Interest> interests, Set<UUID> subscribedInterestIds) {
		return interests.stream()
			.map(interest -> toDto(interest, subscribedInterestIds.contains(interest.getId())))
			.collect(Collectors.toList());
	}
}
