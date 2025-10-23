package com.codeit.monew.interest.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.dto.InterestUpdateRequest;
import com.codeit.monew.interest.service.InterestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
	){
		InterestDto updateInterest = interestService.updateInterest(interestId, request);
		return ResponseEntity.ok(updateInterest);
	}

}