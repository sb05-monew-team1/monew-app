package com.codeit.monew.user.service;

import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;
import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  //Spring Security가 인증을 위해 호출하는 메서드
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    // 1. 이메일로 사용자 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 찾을 수 없습니다: " + email));

    // 2. 논리 삭제된 사용자인지 확인
    if (user.getDeletedAt() != null) {
      throw new UsernameNotFoundException("삭제된 계정입니다: " + email);
    }

    // 3. Spring Security의 UserDetails 객체로 변환하여 반환
    // (권한(Role)이 아직 없다면 빈 리스트를 전달)
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), // Spring Security가 사용할 "username" (여기서는 이메일)
        user.getPassword(), // DB에 저장된 "암호화된" 비밀번호
        Collections.emptyList() // 사용자의 권한(Role) 목록
    );
  }
}