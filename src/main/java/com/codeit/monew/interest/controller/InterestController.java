package com.codeit.monew.interest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.dto.InterestDto;
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

}