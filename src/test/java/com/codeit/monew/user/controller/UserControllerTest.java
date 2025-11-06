package com.codeit.monew.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.codeit.monew.user.dto.UserDto;
import com.codeit.monew.user.dto.UserLoginRequest;
import com.codeit.monew.user.dto.UserRegisterRequest;
import com.codeit.monew.user.dto.UserUpdateRequest;
import com.codeit.monew.user.service.UserService;

class UserControllerTest {

	private final UserService userService = mock(UserService.class);
	private final UserController controller = new UserController(userService);

	@Test
	void registerUserReturnsCreatedResponse() {
		UserRegisterRequest request = new UserRegisterRequest("email@example.com", "nick", "Password1!");
		UserDto userDto = new UserDto(UUID.randomUUID(), request.email(), request.nickname(), Instant.now());
		when(userService.registerUser(request)).thenReturn(userDto);

		ResponseEntity<UserDto> response = controller.registerUser(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(userDto);
	}

	@Test
	void updateUserUsesAuthenticatedUser() {
		UserDto principal = new UserDto(UUID.randomUUID(), "email@example.com", "nick", Instant.now());
		UUID targetId = UUID.randomUUID();
		UserUpdateRequest updateRequest = new UserUpdateRequest("newNick");
		UserDto updated = new UserDto(targetId, "email@example.com", "newNick", Instant.now());
		when(userService.updateUserNickname(principal.id(), targetId, updateRequest)).thenReturn(updated);

		ResponseEntity<UserDto> response = controller.updateUser(principal, targetId, updateRequest);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(updated);
		verify(userService).updateUserNickname(principal.id(), targetId, updateRequest);
	}

	@Test
	void deleteUserReturnsNoContent() {
		UserDto principal = new UserDto(UUID.randomUUID(), "email@example.com", "nick", Instant.now());
		UUID targetId = UUID.randomUUID();

		ResponseEntity<UserDto> response = controller.deleteUser(principal, targetId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(userService).deleteUser(principal.id(), targetId);
	}

	@Test
	void hardDeleteUserReturnsNoContent() {
		UserDto principal = new UserDto(UUID.randomUUID(), "email@example.com", "nick", Instant.now());
		UUID targetId = UUID.randomUUID();

		ResponseEntity<Void> response = controller.hardDeleteUser(principal, targetId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(userService).hardDeleteUser(principal.id(), targetId);
	}

	@Test
	void loginStoresSecurityContextInSession() {
		UserLoginRequest request = new UserLoginRequest("email@example.com", "password");
		UserDto userDto = new UserDto(UUID.randomUUID(), request.email(), "nick", Instant.now());
		when(userService.loginUser(request)).thenReturn(userDto);
		MockHttpSession session = new MockHttpSession();

		ResponseEntity<UserDto> response = controller.login(request, session);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(userDto);
		Object stored = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		assertThat(stored).isInstanceOf(SecurityContext.class);
		verify(userService).loginUser(request);
	}

	@Test
	void homeReturnsGreeting() {
		assertThat(controller.home()).isEqualTo("Monew API Server is running!");
	}
}
