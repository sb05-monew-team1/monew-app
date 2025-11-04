package com.codeit.monew.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;

class OriginValidationFilterTest {
	@Test
	void postRequestWithoutOriginAllowsProcessing() throws ServletException, IOException {
		OriginValidationFilter filter = new OriginValidationFilter(Set.of());
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/123");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(filterChain.getRequest()).isNotNull();
	}

	@Test
	void postRequestFromDisallowedOriginIsRejected() throws ServletException, IOException {
		OriginValidationFilter filter = new OriginValidationFilter(Set.of());
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/123");
		request.addHeader("Origin", "https://evil.com");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(filterChain.getRequest()).isNull();
	}

	@Test
	void postRequestFromAllowedOriginPassesThrough() throws ServletException, IOException {
		OriginValidationFilter filter = new OriginValidationFilter(Set.of("https://client.example.com"));
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/123");
		request.setScheme("https");
		request.setServerName("api.monew.com");
		request.setServerPort(443);
		request.addHeader("Origin", "https://client.example.com");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(filterChain.getRequest()).isNotNull();
	}
}
