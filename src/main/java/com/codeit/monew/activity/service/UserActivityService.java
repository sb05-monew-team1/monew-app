package com.codeit.monew.activity.service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.monew.activity.domain.UserActivity;
import com.codeit.monew.activity.dto.UserActivityDto;
import com.codeit.monew.activity.mapper.UserActivityMapper;
import com.codeit.monew.activity.repository.UserActivityRepository;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserActivityService {

	private final MongoTemplate mongoTemplate;
	private final UserActivityRepository userActivityRepository;
	private final UserActivityMapper  userActivityMapper;

	public UserActivityDto getUserActivityInfo(UUID userId) {
		UserActivity userActivity = userActivityRepository.getUserActivity(userId);

		return userActivityMapper.toUserActivityDto(userActivity);
	}

	@Transactional
	public void createUserActivity(User user) {
		userActivityRepository.createUserActivity(user);
	}

	@Transactional
	public void addSubscription(UUID userId, SubscriptionDto subscription) {
		userActivityRepository.addSubscription(userId, subscription);
	}

	@Transactional
	public void removeSubscription(UUID userId, UUID interestId) {
		userActivityRepository.removeSubscription(userId, interestId);
	}

	@Transactional
	public void addComment(UUID userId, CommentActivityDto comment){
		userActivityRepository.addComment(userId, comment);
	}

	@Transactional
	public void removeComment(UUID userId, UUID commentId) {
		userActivityRepository.removeComment(userId, commentId);
	}

	@Transactional
	public void addCommentLike(UUID userId, CommentLikeActivityDto commentLike) {
		userActivityRepository.addCommentLike(userId, commentLike);
	}

	@Transactional
	public void removeCommentLike(UUID userId, UUID commentLikeId) {
		userActivityRepository.removeCommentLike(userId, commentLikeId);
	}

	@Transactional
	public void addArticleView(UUID userId, ArticleViewDto articleView) {
		userActivityRepository.addArticleView(userId, articleView);
	}

}
