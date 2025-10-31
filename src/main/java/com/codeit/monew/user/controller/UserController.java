package com.codeit.monew.user.controller;

import com.codeit.monew.user.dto.UserDto;
import com.codeit.monew.user.dto.UserLoginRequest;
import com.codeit.monew.user.dto.UserRegisterRequest;
import com.codeit.monew.user.dto.UserUpdateRequest;
import com.codeit.monew.user.service.UserService;
import jakarta.servlet.http.HttpSession;
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
      //@RequestHeader("Monew-Request-User-ID") UUID requestUserId, //헤더추가
      @PathVariable UUID userId,
      @Valid @RequestBody UserUpdateRequest userUpdateRequest,
      HttpSession session
  ) {
    UserDto loggedInUser = (UserDto) session.getAttribute("loggedInUser");
    if(loggedInUser == null){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    UserDto updatedUser = userService.updateUserNickname(loggedInUser.id(), userId, userUpdateRequest);
    return ResponseEntity.ok(updatedUser);
  }

  //회원논리삭제
  @DeleteMapping("/{userId}")
  public ResponseEntity<UserDto> deleteUser(
      //@RequestHeader
      @PathVariable UUID userId,
      HttpSession session
  ) {
    UserDto loggedInUser =  (UserDto) session.getAttribute("loggedInUser");
    if(loggedInUser == null){
      return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    userService.deleteUser(loggedInUser.id(), userId);
    return ResponseEntity.noContent().build(); //204
  }

  //회원물리삭제
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(
      //@RequestHeader("Monew-Request-User-ID") UUID requestUserId, //로깅,감사 등..
      @PathVariable UUID userId,
      HttpSession session
  ) {
    UserDto loggedInUser =  (UserDto) session.getAttribute("loggedInUser");
    if(loggedInUser == null){
      //비로그인 접근 시도(2차방어)
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    //반드시 admin만 호출할 수 있도록 SecurityConfig에서 강력한 권한 제어가 필요.
    userService.hardDeleteUser(loggedInUser.id(), userId);
    return ResponseEntity.noContent().build(); //204
  }

  //로그인
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @Valid @RequestBody UserLoginRequest userLoginRequest,
      HttpSession session
  ) {
    UserDto userDto = userService.loginUser(userLoginRequest);
    //세션에 저장할 키 이름
    final String USER_SESSION_KEY = "loggedInUserId";
    session.setAttribute(USER_SESSION_KEY, userDto.id());
    //세션 타임아웃 설정(필요시)
    //session.setMaxInactiveInterval(7200); //1시간 30분
    return ResponseEntity.ok(userDto);
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
