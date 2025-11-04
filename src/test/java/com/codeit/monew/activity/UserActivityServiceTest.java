package com.codeit.monew.activity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.codeit.monew.activity.domain.UserActivity;
import com.codeit.monew.activity.dto.UserActivityDto;
import com.codeit.monew.activity.service.UserActivityService;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.repository.InterestSubscriptionRepository;
import com.codeit.monew.activity.repository.UserActivityRepository;

@SpringBootTest
@ActiveProfiles("test")
public class UserActivityServiceTest {
	@Autowired
	private InterestSubscriptionRepository interestSubsRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private CommentLikeRepository commentLikeRepository;
	@Autowired
	private ArticleViewRepository articleViewRepository;
	@Autowired
	private UserActivityService userActivityService;
	@MockitoBean
	private UserActivityRepository userActivityRepository;

	@Nested
	class userActivityFieldListTest {
		@Test
		@DisplayName("구독 정보 조회 테스트")
		public void searchSubscriptionsTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<SubscriptionDto> subscriptions = interestSubsRepository.searchSubsCription(userId);

			assertThat(subscriptions).hasSize(1);
			assertThat(subscriptions.get(0).interestName()).isEqualTo("Finance");
			assertThat(subscriptions.get(0).interestKeywords()).containsExactlyInAnyOrder("금융", "주식");
		}

		@Test
		@DisplayName("최근 작성 댓글 조회 테스트")
		public void searchCommentsTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<CommentActivityDto> comments = commentRepository.searchRecentComments(userId);

			assertThat(comments).isNotEmpty();
			assertThat(comments.get(0).content()).isEqualTo("반도체 업황이 회복 중이군요.");
		}

		@Test
		@DisplayName("최근 좋아요 누른 댓글 조회 테스트")
		public void searchCommentLikesTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<CommentLikeActivityDto> commentLikes = commentLikeRepository.searchRecentCommentLikes(userId);

			assertThat(commentLikes).isNotEmpty();
			assertThat(commentLikes.get(0).commentLikeCount()).isGreaterThanOrEqualTo(0);
		}

		@Test
		@DisplayName("최근 본 뉴스 기사 테스트")
		public void searchArticleViewsTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<ArticleViewDto> articleViews = articleViewRepository.searchRecentArticleViews(userId);

			assertThat(articleViews).isNotEmpty();
			assertThat(articleViews.get(0).articleTitle()).isNotBlank();
		}
	}

	@Nested
	class searchUserActivity {
		@Test
		@DisplayName("유저 활동 목록 첫 조회")
		public void createUserActivityTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

			given(userActivityRepository.existsById(userId)).willReturn(false);

			UserActivityDto userActivity = userActivityService.getUserActivityInfo(userId);

			assertThat(userActivity.id()).isEqualTo(userId);
			verify(userActivityRepository).save(any(UserActivity.class));
		}

		@Test
		@DisplayName("유저 활동 목록 mongoDB에서 조회")
		public void searchUserActivityMongoDBTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

			UserActivity persisted = UserActivity.builder()
				.id(userId)
				.email("user1@monew.test")
				.nickname("user1")
				.createdAt(Instant.parse("2024-03-06T09:00:00Z"))
				.subscriptions(List.of())
				.comments(List.of())
				.commentLikes(List.of())
				.articleViews(List.of())
				.build();

			given(userActivityRepository.existsById(userId)).willReturn(true);
			given(userActivityRepository.findById(userId)).willReturn(Optional.of(persisted));

			UserActivityDto userActivity = userActivityService.getUserActivityInfo(userId);

			assertThat(userActivity.nickname()).isEqualTo("user1");
			verify(userActivityRepository, never()).save(any());
		}
	}

	@Test
	@DisplayName("mongodb에서 삭제하는 테스트")
	public void deleteUserActivityMongoDBTest() {
		UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

		given(userActivityRepository.existsById(userId)).willReturn(true);

		userActivityService.deleteUserActivity(userId);

		verify(userActivityRepository).deleteById(userId);
	}

}
