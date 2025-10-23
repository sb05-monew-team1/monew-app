package com.codeit.monew.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    //BCrypt는 단순히 입력을 1회 해시시키는 것이 아니라 솔트를 부여하여
    //여러번 해싱하므로 더 안전하게 암호를 관리할 수 있다.
    return new BCryptPasswordEncoder();
  }
}
