package com.codeit.monew.user.service;

import com.codeit.monew.common.exception.ErrorCode;
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.dto.UserDto;
import com.codeit.monew.user.dto.UserRegisterRequest;
import com.codeit.monew.user.dto.UserUpdateRequest;
import com.codeit.monew.user.exception.UserAlreadyExistsException;
import com.codeit.monew.user.exception.UserException;
import com.codeit.monew.user.mapper.UserMapper;
import com.codeit.monew.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.codeit.monew.user.exception.UserNotFoundException;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  //1. 회원가입
  @Transactional
  public UserDto registerUser(UserRegisterRequest userRegisterRequest){
    //이메일 중복검사
    if (userRepository.existsByEmail(userRegisterRequest.email())) {
      throw new UserAlreadyExistsException(userRegisterRequest.email());
    }

    //닉네임 중복검사(프로토타입엔 x)

    // 비밀번호 암호화
    // DTO에서 받은 평문 비밀번호를 passwordEncoder를 사용해 해시값으로 변경
    String encodedPassword = passwordEncoder.encode(userRegisterRequest.password());

    //엔티티의 정적 팩토리 메서드를 사용해 객체 생성
    User newUser = User.register(
        userRegisterRequest.email(),
        userRegisterRequest.nickname(),
        encodedPassword // 암호화된 비밀번호 전달
    );

    // UserRepository를 통해 DB에 저장
    // save 메서드는 저장된 엔티티(ID, createdAt 등이 채워진)를 반환합니다.
    User savedUser = userRepository.save(newUser);

    //DTO로 변환하여 반환 -> 매퍼 이용
    return userMapper.toUserDto(savedUser);
  }

  //2. 사용자 닉네임 수정
  @Transactional
  public UserDto updateUserNickname(UUID userId, UserUpdateRequest userUpdateRequest){
    //사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    //닉네임 중복 검사 필요시 구현

    //엔티티의 도메인 메서드를 호출하여 닉네임 변경
    user.updateNickname(userUpdateRequest.nickname());

    //반환
    return userMapper.toUserDto(user);
  }

  //3. 사용자 논리 삭제
  @Transactional
  public void deleteUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    //논리삭제 메서드 호출
    user.softDelete();
  }

  //4. 사용자 물리 삭제
  @Transactional
  public void hardDeleteUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    userRepository.deleteById(user.getId());
  }

    /*
  //사용자 정보 조회(필요시 활성화)
  public UserDto getUserInfo(UUID userId){
    //사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    //논리적으로 삭제되지 않은 사용자인지 확인
    if (user.getDeletedAt() != null) {
      throw new UserNotFoundException();
    }

    //DTO로 변환하여 반환
    return userMapper.toUserDto(user);
  }
   */

}
