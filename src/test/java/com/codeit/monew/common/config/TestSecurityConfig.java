package com.codeit.monew.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class TestSecurityConfig {

	@Bean
	public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable());
		return http.build();
	}
}
