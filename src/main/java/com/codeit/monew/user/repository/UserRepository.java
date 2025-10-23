package com.codeit.monew.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.user.domain.User;

//User 엔티티의 데이터 접근..
public interface UserRepository extends JpaRepository<User, UUID> {
	//이메일 중복 검사 및 로그인 시 사용
	Optional<User> findByEmail(String email);

	//회원가입시 이메일 중복 검사
	boolean existsByEmail(String email);

	//닉네임 수정시 중복 검사 (프로토타입엔 없음)

}
