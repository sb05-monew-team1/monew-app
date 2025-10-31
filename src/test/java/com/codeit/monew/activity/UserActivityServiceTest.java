package com.codeit.monew.activity;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.codeit.monew.activity.mapper.UserActivityMapper;
import com.codeit.monew.activity.repository.UserActivityRepository;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.article.repository.ArticleViewRepository;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.comment.repository.CommentLikeRepository;
import com.codeit.monew.comment.repository.CommentRepository;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.interest.repository.InterestSubscriptionRepository;
import com.codeit.monew.user.repository.UserRepository;

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
}
