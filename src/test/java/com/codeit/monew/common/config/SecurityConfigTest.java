package com.codeit.monew.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@WebMvcTest(controllers = SecurityConfigTest.TestUserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.security.allowed-origins=https://allowed.test")
class SecurityConfigTest {
	@Resource
	private MockMvc mockMvc;

	@Test
	void postFromAllowedOriginPassesThroughFilterChain() throws Exception {
		mockMvc.perform(post("/api/users/nickname")
				.header("Origin", "https://allowed.test"))
			.andExpect(status().isOk());
	}

	@Test
	void postFromSameOriginIsPermitted() throws Exception {
		mockMvc.perform(post("/api/users/nickname")
				.header("Origin", "http://localhost"))
			.andExpect(status().isOk());
	}

	@Test
	void postFromDisallowedOriginIsRejected() throws Exception {
		mockMvc.perform(post("/api/users/nickname")
				.header("Origin", "https://evil.test"))
			.andExpect(status().isForbidden());
	}

	@Test
	void getRequestBypassesFilter() throws Exception {
		mockMvc.perform(get("/api/users/ping")
				.header("Origin", "https://evil.test"))
			.andExpect(status().isOk());
	}

	@RestController
	@RequestMapping("/api/users")
	static class TestUserController {
		@PostMapping("/nickname")
		ResponseEntity<Void> updateNickname() {
			return ResponseEntity.ok().build();
		}

		@GetMapping("/ping")
		ResponseEntity<String> ping() {
			return ResponseEntity.ok("pong");
		}
	}
}
