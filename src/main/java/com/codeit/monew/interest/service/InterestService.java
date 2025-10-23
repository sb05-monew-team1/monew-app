package com.codeit.monew.interest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.common.exception.BusinessException;
import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.interest.domain.Interest;
import com.codeit.monew.interest.domain.InterestKeyword;
import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.mapper.InterestMapper;
import com.codeit.monew.interest.repository.InterestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestService {

	private final InterestRepository interestRepository;
	private final InterestMapper interestMapper;

	private static final double SIMILARITY_THRESHOLD = 0.8;

	/**
	 * 관심사 등록
	 */
	@Transactional
	public InterestDto createInterest(InterestRegisterRequest request) {
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

		return interestMapper.toDto(savedInterest, false);
	}

	/**
	 * 관심사 물리 삭제
	 */
	@Transactional
	public void deleteInterest(UUID interestId){
		if (!interestRepository.existsById(interestId)){
			throw new NoSuchElementException("해당 id의 관심사를 찾을 수 없습니다");
		}
		interestRepository.deleteById(interestId);

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