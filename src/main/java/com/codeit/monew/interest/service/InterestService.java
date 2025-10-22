package com.codeit.monew.interest.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.dto.InterestResponse;
import com.codeit.monew.interest.repository.InterestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

	private final InterestRepository interestRepository;

	/**
	 * 관심사 등록
	 */
	@Transactional
	public InterestResponse createInterest(InterestRegisterRequest request) {
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

		return InterestResponse.from(savedInterest);
	}
}