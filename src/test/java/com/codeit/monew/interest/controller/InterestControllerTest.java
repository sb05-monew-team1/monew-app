package com.codeit.monew.interest.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.codeit.monew.common.dto.CursorPageResponse;
import com.codeit.monew.interest.dto.InterestDto;
import com.codeit.monew.interest.dto.InterestRegisterRequest;
import com.codeit.monew.interest.dto.InterestSearchRequest;
import com.codeit.monew.interest.dto.InterestUpdateRequest;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.service.InterestService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = InterestController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = EnableJpaAuditing.class)
	})
@WithMockUser
class InterestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private InterestService interestService;

	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	private final String USER_ID_HEADER = "Monew-Request-User-ID";

	@Nested
	@DisplayName("관심사 등록 (POST /api/interests)")
	class CreateInterestTest {

		@Test
		@DisplayName("성공: 유효한 요청 시 201 Created 상태와 DTO를 반환")
		void createInterest_Success() throws Exception {
			// given
			var request = new InterestRegisterRequest("새 관심사", List.of("키워드1"));
			var responseDto = new InterestDto(UUID.randomUUID(), "새 관심사", List.of("키워드1"), 0L, false);

			when(interestService.createInterest(any(InterestRegisterRequest.class))).thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(post("/api/interests")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			// then
			actions.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(request.name()));
			verify(interestService).createInterest(any(InterestRegisterRequest.class));
		}
	}

	@Nested
	@DisplayName("관심사 삭제 (DELETE /api/interests/{interestId})")
	class DeleteInterestTest {

		@Test
		@DisplayName("성공: 요청 시 204 No Content 상태를 반환")
		void deleteInterest_Success() throws Exception {
			// given
			UUID interestId = UUID.randomUUID();
			doNothing().when(interestService).deleteInterest(interestId);

			// when
			ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}", interestId)
				.with(csrf()));

			// then
			actions.andExpect(status().isNoContent());
			verify(interestService).deleteInterest(interestId);
		}
	}

	@Nested
	@DisplayName("관심사 수정 (PATCH /api/interests/{interestId})")
	class UpdateInterestTest {

		@Test
		@DisplayName("성공: 유효한 요청 시 200 OK 상태와 수정된 DTO를 반환")
		void updateInterest_Success() throws Exception {
			// given
			UUID interestId = UUID.randomUUID();
			var request = new InterestUpdateRequest(List.of("수정된 키워드"));
			var responseDto = new InterestDto(interestId, "기존 이름", List.of("수정된 키워드"), 10L, true);

			when(interestService.updateInterest(eq(interestId), any(InterestUpdateRequest.class))).thenReturn(
				responseDto);

			// when
			ResultActions actions = mockMvc.perform(patch("/api/interests/{interestId}", interestId)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

			// then
			actions.andExpect(status().isOk())
				.andExpect(jsonPath("$.keywords[0]").value("수정된 키워드"));
			verify(interestService).updateInterest(eq(interestId), any(InterestUpdateRequest.class));
		}
	}

	@Nested
	@DisplayName("관심사 목록 조회 (GET /api/interests)")
	class GetInterestsTest {

		@Test
		@DisplayName("성공: 유효한 요청 시 200 OK 상태와 CursorPageResponse의 모든 필드를 포함한 응답을 반환")
		void getInterests_Success() throws Exception {
			// given
			UUID userId = UUID.randomUUID();

			List<InterestDto> contentList = List.of(
				new InterestDto(UUID.randomUUID(), "관심사1", List.of(), 10L, false)
			);

			var responseDto = CursorPageResponse.<InterestDto>builder()
				.content(contentList)
				.nextCursor("next_cursor_value")
				.nextAfter("2025-10-31T12:00:00Z")
				.size(1)
				.totalElements(100L)
				.hasNext(true)
				.build();

			when(interestService.getInterests(any(InterestSearchRequest.class), eq(userId)))
				.thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(get("/api/interests")
				.header(USER_ID_HEADER, userId.toString())
				.param("limit", "1")
				.param("orderBy", "name")
				.param("direction", "ASC"));

			// then
			actions.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content[0].name").value("관심사1"))
				.andExpect(jsonPath("$.nextCursor").value("next_cursor_value"))
				.andExpect(jsonPath("$.nextAfter").value("2025-10-31T12:00:00Z"))
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.totalElements").value(100L))
				.andExpect(jsonPath("$.hasNext").value(true));

			verify(interestService).getInterests(any(InterestSearchRequest.class), eq(userId));
		}
	}

	@Nested
	@DisplayName("관심사 구독 (POST /api/interests/{interestId}/subscriptions)")
	class CreateSubscriptionTest {

		@Test
		@DisplayName("성공: 유효한 요청 시 200 OK 상태와 구독 DTO를 반환")
		void createSubscription_Success() throws Exception {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();
			var responseDto = new SubscriptionDto(UUID.randomUUID(), interestId, "구독한 관심사", List.of(), 1L, null);

			when(interestService.createSubscription(interestId, userId)).thenReturn(responseDto);

			// when
			ResultActions actions = mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
				.with(csrf())
				.header(USER_ID_HEADER, userId.toString()));

			// then
			actions.andExpect(status().isOk())
				.andExpect(jsonPath("$.interestId").value(interestId.toString()));
			verify(interestService).createSubscription(interestId, userId);
		}
	}

	@Nested
	@DisplayName("관심사 구독 취소 (DELETE /api/interests/{interestId}/subscriptions)")
	class DeleteSubscriptionTest {

		@Test
		@DisplayName("성공: 유효한 요청 시 204 No Content 상태를 반환")
		void deleteSubscription_Success() throws Exception {
			// given
			UUID userId = UUID.randomUUID();
			UUID interestId = UUID.randomUUID();
			doNothing().when(interestService).deleteSubscription(interestId, userId);

			// when
			ResultActions actions = mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
				.with(csrf())
				.header(USER_ID_HEADER, userId.toString()));

			// then
			actions.andExpect(status().isNoContent());
			verify(interestService).deleteSubscription(interestId, userId);
		}
	}
}