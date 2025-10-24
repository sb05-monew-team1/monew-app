package com.codeit.monew.user.controller;

import com.codeit.monew.user.dto.UserDto;
import com.codeit.monew.user.dto.UserRegisterRequest;
import com.codeit.monew.user.dto.UserUpdateRequest;
import com.codeit.monew.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  //회원가입
  @PostMapping
  public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
    UserDto userDto = userService.registerUser(userRegisterRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  //닉네임수정
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID userId,
      @Valid @RequestBody UserUpdateRequest userUpdateRequest
  ) {
    UserDto updatedUser = userService.updateUserNickname(userId, userUpdateRequest);
    return ResponseEntity.ok(updatedUser);
  }

  //회원논리삭제
  @DeleteMapping("/{userId}")
  public ResponseEntity<UserDto> deleteUser(@PathVariable UUID userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build(); //204
  }

  //회원물리삭제
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(@PathVariable UUID userId) {
    //반드시 admin만 호출할 수 있도록 SecurityConfig에서 강력한 권한 제어가 필요.
    userService.hardDeleteUser(userId);
    return ResponseEntity.noContent().build(); //204
  }

  /*
  //회원정보조회(필요시 활성화)
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUserInfo(@PathVariable UUID userId) {
    UserDto userDto = userService.getUserInfo(userId);
    return ResponseEntity.ok(userDto);
  }
   */

}
