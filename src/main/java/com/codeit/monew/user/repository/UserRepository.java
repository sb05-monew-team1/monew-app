package com.codeit.monew.user.repository;

import com.codeit.monew.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

//User 엔티티의 데이터 접근..
public interface UserRepository extends JpaRepository<User, Long> {
  //이메일 중복 검사 및 로그인 시 사용
  Optional<User> findByEmail(String email);

  //회원가입시 이메일 중복 검사
  boolean existsByEmail(String email);

  //닉네임 수정시 중복 검사 (프로토타입엔 없음)
}
