package com.codeit.monew.common.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import com.codeit.monew.common.security.OriginValidationFilter;

@Configuration
public class SecurityConfig {
	@Bean
	public PasswordEncoder passwordEncoder() {
		//BCrypt는 단순히 입력을 1회 해시시키는 것이 아니라 솔트를 부여하여
		//여러번 해싱하므로 더 안전하게 암호를 관리할 수 있다.
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, OriginValidationFilter originValidationFilter) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			)
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
				.requestMatchers("/api/users/login").permitAll()
				.anyRequest().permitAll()
			);
		// (로그인/로그아웃 폼 비활성화)
		http.formLogin(AbstractHttpConfigurer::disable);
		http.httpBasic(AbstractHttpConfigurer::disable); // (선택) Basic Auth 비활성화
		http.addFilterBefore(originValidationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public OriginValidationFilter originValidationFilter(
		@Value("${app.security.allowed-origins:}") String allowedOriginsProperty
	) {
		if (!StringUtils.hasText(allowedOriginsProperty)) {
			return new OriginValidationFilter(Collections.emptySet());
		}

		Set<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.collect(Collectors.toSet());
		return new OriginValidationFilter(allowedOrigins);
	}
}
