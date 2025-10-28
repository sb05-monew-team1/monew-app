package com.codeit.monew.user.repository;

import com.codeit.monew.user.domain.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

//User 엔티티의 데이터 접근..
public interface UserRepository extends JpaRepository<User, UUID> {
  //Spring Security 및 로그인 처리를 위해 이메일로 사용자 조회
  Optional<User> findByEmail(String email);

  //회원가입시 이메일 중복 검사
  boolean existsByEmail(String email);

  //닉네임 수정시 중복 검사 (프로토타입엔 없음)

  @Transactional
  void deleteByDeletedAtBefore(Instant cutoffTime);
}
