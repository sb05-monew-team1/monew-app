package com.codeit.monew.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public PasswordEncoder passwordEncoder() {
		//BCrypt는 단순히 입력을 1회 해시시키는 것이 아니라 솔트를 부여하여
		//여러번 해싱하므로 더 안전하게 암호를 관리할 수 있다.
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // CSRF는 비활성화 유지 (대신 쿠키 설정으로 방어)
			.sessionManagement(session ->
				//STATELESS가 아닌, 세션을 사용하도록 명시
				//IF_REQUIRED: 스프링 시큐리티가 필요시 세션을 생성 (로그인 시)
				session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
				.requestMatchers("/api/users/login").permitAll()
				.anyRequest().authenticated()
			);
		// (로그인/로그아웃 폼 비활성화)
		http.formLogin(form -> form.disable());
		http.httpBasic(basic -> basic.disable()); // (선택) Basic Auth 비활성화

		return http.build();
	}
}
