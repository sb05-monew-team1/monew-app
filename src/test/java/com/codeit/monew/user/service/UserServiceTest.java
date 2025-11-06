package com.codeit.monew.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.dto.UserDto;
import com.codeit.monew.user.dto.UserLoginRequest;
import com.codeit.monew.user.dto.UserRegisterRequest;
import com.codeit.monew.user.dto.UserUpdateRequest;
import com.codeit.monew.user.exception.UserAlreadyDeletedException;
import com.codeit.monew.user.exception.UserAlreadyExistsException;
import com.codeit.monew.user.exception.UserForbiddenException;
import com.codeit.monew.user.exception.UserLoginFailedException;
import com.codeit.monew.user.exception.UserNotFoundException;
import com.codeit.monew.user.exception.UserNotSoftDeletedException;
import com.codeit.monew.user.mapper.UserMapper;
import com.codeit.monew.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserService userService;

	@Test
	void registerUserEncodesPasswordAndReturnsDto() {
		UserRegisterRequest request = new UserRegisterRequest("user@example.com", "nickname", "PlainPwd1!");
		User savedUser = User.register("user@example.com", "nickname", "encoded");
		ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());
		UserDto expectedDto = mock(UserDto.class);

		when(userRepository.existsByEmail(request.email())).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded");
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toUserDto(savedUser)).thenReturn(expectedDto);

		UserDto result = userService.registerUser(request);

		assertThat(result).isSameAs(expectedDto);
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded");
	}

	@Test
	void registerUserThrowsWhenEmailExists() {
		UserRegisterRequest request = new UserRegisterRequest("user@example.com", "nickname", "PlainPwd1!");
		when(userRepository.existsByEmail(request.email())).thenReturn(true);

		assertThatThrownBy(() -> userService.registerUser(request))
			.isInstanceOf(UserAlreadyExistsException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserNicknameUpdatesNickname() {
		UUID userId = UUID.randomUUID();
		UserUpdateRequest updateRequest = new UserUpdateRequest("newNickname");
		User user = User.register("user@example.com", "oldNickname", "encoded");
		ReflectionTestUtils.setField(user, "id", userId);
		UserDto expectedDto = mock(UserDto.class);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userMapper.toUserDto(user)).thenReturn(expectedDto);

		UserDto result = userService.updateUserNickname(userId, userId, updateRequest);

		assertThat(result).isSameAs(expectedDto);
		assertThat(user.getNickname()).isEqualTo("newNickname");
	}

	@Test
	void updateUserNicknameThrowsWhenRequesterDiffers() {
		UUID requester = UUID.randomUUID();
		UUID target = UUID.randomUUID();

		assertThatThrownBy(() -> userService.updateUserNickname(requester, target, new UserUpdateRequest("nick")))
			.isInstanceOf(UserForbiddenException.class);

		verify(userRepository, never()).findById(any());
	}

	@Test
	void updateUserNicknameThrowsWhenUserDeleted() {
		UUID userId = UUID.randomUUID();
		User user = User.register("user@example.com", "nick", "encoded");
		user.softDelete();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.updateUserNickname(userId, userId, new UserUpdateRequest("nick")))
			.isInstanceOf(UserAlreadyDeletedException.class);
	}

	@Test
	void deleteUserSoftDeletesAccount() {
		UUID userId = UUID.randomUUID();
		User user = User.register("user@example.com", "nick", "encoded");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		userService.deleteUser(userId, userId);

		assertThat(user.getDeletedAt()).isNotNull();
	}

	@Test
	void deleteUserThrowsForDifferentRequesters() {
		assertThatThrownBy(() -> userService.deleteUser(UUID.randomUUID(), UUID.randomUUID()))
			.isInstanceOf(UserForbiddenException.class);
	}

	@Test
	void deleteUserThrowsWhenAlreadyDeleted() {
		UUID userId = UUID.randomUUID();
		User user = User.register("user@example.com", "nick", "encoded");
		user.softDelete();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.deleteUser(userId, userId))
			.isInstanceOf(UserAlreadyDeletedException.class);
	}

	@Test
	void hardDeleteUserDeletesWhenSoftDeleted() {
		UUID userId = UUID.randomUUID();
		User user = User.register("user@example.com", "nick", "encoded");
		user.softDelete();
		ReflectionTestUtils.setField(user, "id", userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		userService.hardDeleteUser(userId, userId);

		verify(userRepository).deleteById(userId);
	}

	@Test
	void hardDeleteUserThrowsWhenNotSoftDeleted() {
		UUID userId = UUID.randomUUID();
		User user = User.register("user@example.com", "nick", "encoded");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.hardDeleteUser(userId, userId))
			.isInstanceOf(UserNotSoftDeletedException.class);

		verify(userRepository, never()).deleteById(any());
	}

	@Test
	void loginUserReturnsDtoWhenCredentialsMatch() {
		UserLoginRequest request = new UserLoginRequest("user@example.com", "password");
		User user = User.register("user@example.com", "nick", "encoded");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
		UserDto dto = mock(UserDto.class);
		when(userMapper.toUserDto(user)).thenReturn(dto);

		UserDto result = userService.loginUser(request);

		assertThat(result).isSameAs(dto);
	}

	@Test
	void loginUserThrowsWhenUserDeleted() {
		UserLoginRequest request = new UserLoginRequest("user@example.com", "password");
		User user = User.register("user@example.com", "nick", "encoded");
		ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
		user.softDelete();
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.loginUser(request))
			.isInstanceOf(UserAlreadyDeletedException.class);
	}

	@Test
	void loginUserThrowsWhenPasswordMismatch() {
		UserLoginRequest request = new UserLoginRequest("user@example.com", "password");
		User user = User.register("user@example.com", "nick", "encoded");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(eq(request.password()), eq(user.getPassword()))).thenReturn(false);

		assertThatThrownBy(() -> userService.loginUser(request))
			.isInstanceOf(UserLoginFailedException.class);
	}

	@Test
	void loginUserThrowsWhenUserMissing() {
		UserLoginRequest request = new UserLoginRequest("missing@example.com", "password");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.loginUser(request))
			.isInstanceOf(UserNotFoundException.class);
	}
}
