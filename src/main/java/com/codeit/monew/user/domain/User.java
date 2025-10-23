package com.codeit.monew.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED) //접근 레벨 PROTECTED
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true) // 'nullable = false': NOT NULL, 'unique = true': UNIQUE 제약조건
  private String email;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  private String password; //암호화 되어야 함.

  @CreatedDate
  @Column(nullable = false, updatable = false) //생성 이후 수정불가
  private LocalDateTime createdAt;

  @LastModifiedDate //엔티티 수정 시각 자동저장
  @Column(nullable = false)
  private LocalDateTime lastModifiedAt;

  //논리 삭제
  private LocalDateTime deleteAt;

  //물리 삭제
  private LocalDateTime physicalDeleteAt;

  //회원가입을 위한 User 객체 생성
  public User user(String email, String nickname, String password, LocalDateTime createdAt, LocalDateTime lastModifiedAt) {
    User user = new User();
    user.email = email;
    user.nickname = nickname;
    user.password = password;
    user.createdAt = createdAt;
    user.lastModifiedAt = lastModifiedAt;
    return user;
  }

  //비지니스 로직 메서드
  //닉네임 수정
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  //논리 삭제 처리
  //논리삭제시 1일 뒤 물리 삭제
  public void softDelete(LocalDateTime physicalDeleteAt) {
    this.deleteAt = LocalDateTime.now();
    this.physicalDeleteAt = physicalDeleteAt;

  }
}
