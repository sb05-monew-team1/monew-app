package com.codeit.monew.user.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserDtosTest {

	@Test
	void userDtoHoldsValues() {
		UUID id = UUID.randomUUID();
		Instant createdAt = Instant.now();

		UserDto dto = new UserDto(id, "email@example.com", "nickname", createdAt);

		assertThat(dto.id()).isEqualTo(id);
		assertThat(dto.email()).isEqualTo("email@example.com");
		assertThat(dto.nickname()).isEqualTo("nickname");
		assertThat(dto.createdAt()).isEqualTo(createdAt);
	}

	@Test
	void userLoginRequestHoldsValues() {
		UserLoginRequest request = new UserLoginRequest("email@example.com", "password");

		assertThat(request.email()).isEqualTo("email@example.com");
		assertThat(request.password()).isEqualTo("password");
	}

	@Test
	void userLoginResponseHoldsToken() {
		UserLoginResponse response = new UserLoginResponse("token");

		assertThat(response.accessToken()).isEqualTo("token");
	}

	@Test
	void userRegisterRequestHoldsValues() {
		UserRegisterRequest request = new UserRegisterRequest("email@example.com", "nickname", "password1!");

		assertThat(request.email()).isEqualTo("email@example.com");
		assertThat(request.nickname()).isEqualTo("nickname");
		assertThat(request.password()).isEqualTo("password1!");
	}

	@Test
	void userUpdateRequestHoldsNickname() {
		UserUpdateRequest request = new UserUpdateRequest("newNickname");

		assertThat(request.nickname()).isEqualTo("newNickname");
	}
}
