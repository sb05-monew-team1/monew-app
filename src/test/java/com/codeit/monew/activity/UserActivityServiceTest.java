package com.codeit.monew.activity;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

@SpringBootTest
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

	@Nested
	class userActivityFieldListTest {
		@Test
		@DisplayName("구독 정보 조회 테스트")
		public void searchSubscriptionsTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<SubscriptionDto> subscriptions = interestSubsRepository.searchSubsCription(userId);

			System.out.println("구독한 관심사 정보: ");
			subscriptions.forEach(System.out::println);
		}

		@Test
		@DisplayName("최근 작성 댓글 조회 테스트")
		public void searchCommentsTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<CommentActivityDto> comments = commentRepository.searchRecentComments(userId);
			System.out.println("최근 작성 댓글: ");
			comments.forEach(System.out::println);
		}

		@Test
		@DisplayName("최근 좋아요 누른 댓글 조회 테스트")
		public void searchCommentLikesTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<CommentLikeActivityDto> commentLikes = commentLikeRepository.searchRecentCommentLikes(userId);
			System.out.println("최근 좋아요 누른 댓글: ");
			commentLikes.forEach(System.out::println);
		}

		@Test
		@DisplayName("최근 본 뉴스 기사 테스트")
		public void searchArticleViewsTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			List<ArticleViewDto> articleViews = articleViewRepository.searchRecentArticleViews(userId);
			System.out.println("최근 본 뉴스 기사: ");
			articleViews.forEach(System.out::println);
		}
	}

	@Nested
	class searchUserActivity {
		@Test
		@DisplayName("유저 활동 목록 첫 조회")
		public void createUserActivityTest() {
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			UserActivityDto userActivity = userActivityService.getUserActivityInfo(userId);
			System.out.println("유저 활동 조회 : " + userActivity.toString());
		}

		@Test
		@DisplayName("유저 활동 목록 mongoDB에서 조회")
		public void searchUserActivityMongoDBTest() {
			System.out.println("createUserActivityTest를 통해 이미 db에 저장되었다고 가정(디버깅을 통해 확인).");
			UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
			UserActivityDto userActivity = userActivityService.getUserActivityInfo(userId);
			System.out.println("유저 활동 조회(from mongodb) : " + userActivity.toString());
		}
	}

	@Test
	@DisplayName("mongodb에서 삭제하는 테스트")
	public void deleteUserActivityMongoDBTest() {
		System.out.println("삭제: ");
		UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		userActivityService.deleteUserActivity(userId);
	}

}
