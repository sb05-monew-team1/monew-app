package com.codeit.monew.article;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.article.exception.ArticleViewAlreadyExistException;
import com.codeit.monew.article.service.ArticleService;
import com.codeit.monew.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ArticleApiIntegrationTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ArticleService articleService;

	@Autowired
	private MockMvc mockMvc;

	@Nested
	class RegisterArticleView {
		static UUID userId;
		static UUID articleId;

		@Test
		@DisplayName("기사 뷰 등록 성공")
		void success() throws Exception {
			articleId = UUID.fromString("10000000-0000-0000-0000-000000000004");
			userId = UUID.fromString("22222222-2222-2222-2222-222222222222");

			mockMvc.perform(
					post("/api/articles/{articleId}/article-views", articleId)
						.header("Monew-Request-User-ID", userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.viewedBy").value(userId.toString()))
				.andExpect(jsonPath("$.articleId").value(articleId.toString()))
				.andExpect(jsonPath("$.createdAt").exists());
		}

		@Test
		@DisplayName("기사 뷰 등록 실패 - 이미 존재하는 기사 뷰")
		void fail_article_view_already_exists() throws Exception {
			articleId = UUID.fromString("10000000-0000-0000-0000-000000000001");
			userId = UUID.fromString("22222222-2222-2222-2222-222222222222");

			mockMvc.perform(
					post("/api/articles/{articleId}/article-views", articleId)
						.header("Monew-Request-User-ID", userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value(ErrorCode.ARTICLE_VIEW_ALREADY_EXIST.getMessage()))
				.andExpect(jsonPath("$.code").value(ErrorCode.ARTICLE_VIEW_ALREADY_EXIST.name()))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.details.articleId").value(articleId.toString()))
				.andExpect(jsonPath("$.details.userId").value(userId.toString()))
				.andExpect(jsonPath("$.exceptionType").value(ArticleViewAlreadyExistException.class.getSimpleName()));
		}

		@Test
		@DisplayName("기사 뷰 등록 실패 - 존재하지 않는 사용자")
		void fail_user_not_found() throws Exception {
			articleId = UUID.fromString("10000000-0000-0000-0000-000000000001");
			userId = UUID.randomUUID();

			mockMvc.perform(
					post("/api/articles/{articleId}/article-views", articleId)
						.header("Monew-Request-User-ID", userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}

		@Test
		@DisplayName("기사 뷰 등록 실패 - 존재하지 않는 기사")
		void fail_article_not_found() throws Exception {
			articleId = UUID.randomUUID();
			userId = UUID.fromString("22222222-2222-2222-2222-222222222222");

			mockMvc.perform(
					post("/api/articles/{articleId}/article-views", articleId)
						.header("Monew-Request-User-ID", userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}
	}

	@Nested
	class GetArticles {

		@Test
		@DisplayName("기사 목록 조회 성공")
		void success() throws Exception {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

			mockMvc.perform(
					get("/api/articles")
						.header("Monew-Request-User-ID", userId)
						.queryParam("orderBy", "publishDate")
						.queryParam("direction", "DESC")
						.queryParam("limit", "10"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
				.andExpect(jsonPath("$.content[0].id").exists())
				.andExpect(jsonPath("$.content[0].title").isString())
				.andExpect(jsonPath("$.content[0].viewedByMe").isBoolean())
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.hasNext").isBoolean());
		}

		@Test
		@DisplayName("요청자 ID 헤더 누락 시 400 반환")
		void missingHeader() throws Exception {
			mockMvc.perform(
					get("/api/articles")
						.queryParam("orderBy", "publishDate")
						.queryParam("direction", "DESC")
						.queryParam("limit", "10"))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("존재하지 않는 사용자로 조회 시 404 반환")
		void userNotFound() throws Exception {
			UUID unknownUserId = UUID.randomUUID();

			mockMvc.perform(
					get("/api/articles")
						.header("Monew-Request-User-ID", unknownUserId)
						.queryParam("orderBy", "publishDate")
						.queryParam("direction", "DESC")
						.queryParam("limit", "10"))
				.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("정렬 방향 파라미터가 잘못되면 400 반환")
		void invalidDirection() throws Exception {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

			mockMvc.perform(
					get("/api/articles")
						.header("Monew-Request-User-ID", userId)
						.queryParam("orderBy", "publishDate")
						.queryParam("direction", "WRONG")
						.queryParam("limit", "10"))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	class GetArticle {
		static UUID articleId = UUID.fromString("10000000-0000-0000-0000-000000000001");
		static UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

		@Test
		@DisplayName("기사 단건 조회 성공(200)")
		void success() throws Exception {
			mockMvc.perform(
					get("/api/articles/{articleId}", articleId)
						.header("Monew-Request-User-ID", userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}

		@Test
		@DisplayName("기사 단건 조회 실패 - 존재하지 않는 기사(404)")
		void article_not_found() throws Exception {
			mockMvc.perform(
					get("/api/articles/{articleId}", UUID.randomUUID())
						.header("Monew-Request-User-ID", userId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}

		@Test
		@DisplayName("기사 단건 조회 실패 - 존재하지 않는 사용자(404)")
		void user_not_found() throws Exception {
			mockMvc.perform(
					get("/api/articles/{articleId}", articleId)
						.header("Monew-Request-User-ID", UUID.randomUUID())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}
	}

	@Nested
	class DeleteArticleSoft {
		static UUID articleId = UUID.fromString("10000000-0000-0000-0000-000000000001");

		@Test
		@DisplayName("기사 논리 삭제 성공(204)")
		void success() throws Exception {
			mockMvc.perform(
					delete("/api/articles/{articleId}", articleId))
				.andExpect(status().isNoContent());
		}

		@Test
		@DisplayName("기사 논리 삭제 실패 - 존재하지 않는 기사(404)")
		void article_not_found() throws Exception {
			mockMvc.perform(
					delete("/api/articles/{articleId}", UUID.randomUUID())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}

		@Test
		@DisplayName("기사 논리 삭제 실패 - 논리 삭제된 기사(404)")
		void article_not_found2() throws Exception {
			articleService.deleteSoft(articleId);

			mockMvc.perform(
					delete("/api/articles/{articleId}", articleId)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}
	}

	@Nested
	class DeleteArticleHard {
		static UUID articleId = UUID.fromString("10000000-0000-0000-0000-000000000001");

		@Test
		@DisplayName("기사 물리 삭제 성공(204)")
		void success() throws Exception {
			mockMvc.perform(
					delete("/api/articles/{articleId}/hard", articleId))
				.andExpect(status().isNoContent());
		}

		@Test
		@DisplayName("기사 물리 삭제 성공 - 논리 삭제된 기사(204)")
		void success2() throws Exception {
			articleService.deleteSoft(articleId);
			mockMvc.perform(
					delete("/api/articles/{articleId}/hard", articleId))
				.andExpect(status().isNoContent());
		}

		@Test
		@DisplayName("기사 물리 삭제 실패 - 존재하지 않는 기사(404)")
		void article_not_found() throws Exception {
			mockMvc.perform(
					delete("/api/articles/{articleId}/hard", UUID.randomUUID())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}
	}

	@Nested
	class GetSources {
		@Test
		@DisplayName("출처 목록 조회")
		void success() throws Exception {
			mockMvc.perform(
				get("/api/articles/sources")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
		}
	}
}
