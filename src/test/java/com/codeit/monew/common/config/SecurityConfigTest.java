package com.codeit.monew.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

class SecurityConfigTest {

	private final SecurityConfig securityConfig = new SecurityConfig();

	@Test
	void passwordEncoderReturnsBcryptEncoder() {
		PasswordEncoder encoder = securityConfig.passwordEncoder();

		assertThat(encoder).isNotNull();
		assertThat(encoder.encode("password")).isNotEqualTo("password");
	}

	@Test
	void filterChainConfiguresHttpSecurity() throws Exception {
		HttpSecurity http = mock(HttpSecurity.class);
		DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);

		when(http.csrf(any())).thenReturn(http);
		when(http.sessionManagement(any())).thenReturn(http);
		when(http.authorizeHttpRequests(any())).thenReturn(http);
		when(http.formLogin(any())).thenReturn(http);
		when(http.httpBasic(any())).thenReturn(http);
		when(http.build()).thenReturn(chain);

		SecurityFilterChain result = securityConfig.filterChain(http);

		assertThat(result).isSameAs(chain);
		verify(http).csrf(any());
		verify(http).sessionManagement(any());
		verify(http).authorizeHttpRequests(any());
		verify(http).formLogin(any());
		verify(http).httpBasic(any());
		verify(http).build();
	}
}
