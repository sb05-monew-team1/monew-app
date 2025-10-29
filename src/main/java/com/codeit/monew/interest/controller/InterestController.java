package com.codeit.monew.interest.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.dto.InterestSearchRequest;
import com.codeit.monew.interest.dto.InterestUpdateRequest;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.service.InterestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

	private final InterestService interestService;

	/**
	 * 관심사 등록
	 */
	@PostMapping
	public ResponseEntity<InterestDto> createInterest(
		@Valid @RequestBody InterestRegisterRequest request
	) {
		log.info("POST /api/interests");

		InterestDto response = interestService.createInterest(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 관심사 물리 삭제
	 */
	@DeleteMapping("{interestId}")
	public ResponseEntity<Void> deleteInterest(
		@PathVariable UUID interestId
	) {
		log.info("DELETE /api/interests/{}", interestId);

		interestService.deleteInterest(interestId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * 관심사 정보 수정
	 */
	@PatchMapping("/{interestId}")
	public ResponseEntity<InterestDto> updateInterest(
		@PathVariable UUID interestId,
		@Valid @RequestBody InterestUpdateRequest request
	) {
		log.info("PATCH /api/interests/{}", interestId);

		InterestDto updateInterest = interestService.updateInterest(interestId, request);
		return ResponseEntity.ok(updateInterest);
	}

	/**
	 * 관심사 목록 조회
	 */
	@GetMapping
	public ResponseEntity<CursorPageResponse<InterestDto>> getInterest(
		@Valid @ModelAttribute InterestSearchRequest request,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		log.info("GET /api/interests");

		CursorPageResponse<InterestDto> response = interestService.getInterests(request, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 관심사 구독
	 */
	@PostMapping("{interestId}/subscriptions")
	public ResponseEntity<SubscriptionDto> createSubscription(
		@PathVariable UUID interestId,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		log.info("POST /api/interests/{}/subscriptions", interestId);

		SubscriptionDto response = interestService.createSubscription(interestId, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 관심사 구독 취소
	 */
	@DeleteMapping("{interestId}/subscriptions")
	public ResponseEntity<Void> deleteSubscription(
		@PathVariable UUID interestId,
		@RequestHeader("Monew-Request-User-ID") UUID userId
	) {
		log.info("DELETE /api/interests/{}/subscriptions", interestId);

		interestService.deleteSubscription(interestId, userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}