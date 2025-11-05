package com.codeit.monew.activity.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.monew.activity.dto.UserActivityDto;
import com.codeit.monew.activity.service.UserActivityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-activities")
public class UserActivityController {

	private final UserActivityService userActivityService;

	@GetMapping("/{userId}")
	public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable String userId) {
		log.info("GET /api/user-activities/{}", userId);

		return ResponseEntity.ok(userActivityService.getUserActivityInfo(UUID.fromString(userId)));
	}
}
