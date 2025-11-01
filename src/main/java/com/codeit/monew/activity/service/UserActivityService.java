package com.codeit.monew.activity.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codeit.monew.activity.domain.UserActivity;
import com.codeit.monew.activity.dto.UserActivityDto;
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
import com.codeit.monew.user.domain.User;
import com.codeit.monew.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserActivityService {

	private final UserRepository userRepository;
	private final UserActivityRepository userActivityRepository;
	private final UserActivityMapper userActivityMapper;
	private final InterestSubscriptionRepository interestSubsRepository;
	private final CommentRepository commentRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final ArticleViewRepository articleViewRepository;

	public UserActivityDto getUserActivityInfo(UUID userId) {
		if (userActivityRepository.existsById(userId)) {
			UserActivity userActivity = userActivityRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("userActivity not found"));
			return userActivityMapper.toUserActivityDto(userActivity);
		}

		UserActivity userActivity = createUserActivity(userId);
		userActivityRepository.save(userActivity);

		return userActivityMapper.toUserActivityDto(userActivity);
	}

	public void deleteUserActivity(UUID userId) {
		if (userActivityRepository.existsById(userId)) {
			userActivityRepository.deleteById(userId);
		}
	}

	private UserActivity createUserActivity(UUID userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));

		List<SubscriptionDto> subscriptions = interestSubsRepository.searchSubsCription(userId);
		List<CommentActivityDto> comments = commentRepository.searchRecentComments(userId);
		List<CommentLikeActivityDto> commentLikes = commentLikeRepository.searchRecentCommentLikes(userId);
		List<ArticleViewDto> articleViews = articleViewRepository.searchRecentArticleViews(userId);
		return UserActivity
			.builder()
			.id(userId)
			.email(user.getEmail())
			.nickname(user.getNickname())
			.createdAt(Instant.now())
			.subscriptions(subscriptions)
			.comments(comments)
			.commentLikes(commentLikes)
			.articleViews(articleViews)
			.build();
	}

}
