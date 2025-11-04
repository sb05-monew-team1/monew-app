package com.codeit.monew.user.controller;

import com.codeit.monew.user.dto.UserDto;
import com.codeit.monew.user.dto.UserLoginRequest;
import com.codeit.monew.user.dto.UserRegisterRequest;
import com.codeit.monew.user.dto.UserUpdateRequest;
import com.codeit.monew.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
      @AuthenticationPrincipal UserDto loggedInUser,
      @PathVariable UUID userId,
      @Valid @RequestBody UserUpdateRequest userUpdateRequest
      //HttpSession session
  ) {
    UserDto updatedUser = userService.updateUserNickname(loggedInUser.id(), userId, userUpdateRequest);
    return ResponseEntity.ok(updatedUser);
  }

  //회원논리삭제
  @DeleteMapping("/{userId}")
  public ResponseEntity<UserDto> deleteUser(
      //@RequestHeader
      @AuthenticationPrincipal UserDto loggedInUser,
      @PathVariable UUID userId
      //HttpSession session
  ) {
    userService.deleteUser(loggedInUser.id(), userId);
    return ResponseEntity.noContent().build(); //204
  }

  //회원물리삭제
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDeleteUser(
      //@RequestHeader("Monew-Request-User-ID") UUID requestUserId, //로깅,감사 등..
      @AuthenticationPrincipal UserDto loggedInUser,
      @PathVariable UUID userId
      //HttpSession session
  ) {
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

    //인증설정
    //1. 권한 설정(기본 "role_user")
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    //2. 객체 생성
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDto, // Principal (로그인된 사용자 정보)
        null,    // Credentials (비밀번호, 인증 후엔 null 처리)
        authorities // Authorities (권한 목록)
    );

    // 1-3. SecurityContext 생성 및 Authentication 객체 저장
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);

    // 4. SecurityContext를 HttpSession에 저장
    //    이후 요청부터 Spring Security가 이 세션을 읽어 인증 여부를 판단합니다.
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

    //세션에 저장할 키 이름
    //final String USER_SESSION_KEY = "loggedInUser";
    //session.setAttribute(USER_SESSION_KEY, userDto);

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
