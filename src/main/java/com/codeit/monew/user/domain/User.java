package com.codeit.monew.user.domain;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.monew.common.base.BaseUpdatableDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

;

@Getter
@Entity
@Table(name = "users")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED) //접근 레벨 PROTECTED
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseUpdatableDomain {

	@Column(nullable = false, unique = true) // 'nullable = false': NOT NULL, 'unique = true': UNIQUE 제약조건
	private String email;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String password; //암호화 되어야 함.

	//논리 삭제시간
	private Instant deletedAt;

	//회원가입을 위한 User 객체 생성
	public static User register(String email, String nickname, String password) {
		return User.builder()
			.email(email)
			.nickname(nickname)
			.password(password)
			// id, createdAt, updatedAt은 부모 클래스와 Auditing이 자동 처리
			.build();
	}

	//비지니스 로직 메서드
	//닉네임 수정
	public void updateNickname(String nickname) {

		this.nickname = nickname;
	}

	//논리 삭제 처리
	//논리삭제시 1일 뒤 물리 삭제
	public void softDelete() {
		this.deletedAt = Instant.now();
	}
}
