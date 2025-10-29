package com.codeit.monew.activity.repository;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.codeit.monew.activity.domain.UserActivity;
import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.interest.dto.SubscriptionDto;
import com.codeit.monew.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserActivityQueryRepositoryImpl implements UserActivityQueryRepository {

	private final MongoTemplate mongoTemplate;

	@Override
	public UserActivity getUserActivity(UUID userId) {
		Query query = new Query(Criteria.where("_id").is(userId));

		query.fields()
			.include("_id")
			.include("email")
			.include("nickname")
			.include("createdAt")
			.include("subscriptions")
			.slice("comments", 10)
			.slice("commentLikes", 10)
			.slice("articleViews", 10);

		UserActivity userActivity = mongoTemplate.findOne(query, UserActivity.class);
		if (userActivity != null) {
			throw new RuntimeException("유저의 활동 내역이 존재하지 않습니다.");
		}

		return userActivity;
	}

	@Override
	public void createUserActivity(User user) {
		UserActivity userActivity = new UserActivity();
		userActivity.setId(user.getId());
		userActivity.setEmail(user.getEmail());
		userActivity.setNickname(user.getNickname());
		userActivity.setCreatedAt(Instant.now());
		userActivity.setSubscriptions(Collections.emptyList());
		userActivity.setComments(Collections.emptyList());
		userActivity.setCommentLikes(Collections.emptyList());
		userActivity.setArticleViews(Collections.emptyList());

		mongoTemplate.insert(userActivity);
	}

	@Override
	public void addSubscription(UUID userId, SubscriptionDto subscription) {
		Query query = Query.query(Criteria.where("_id").is(userId)
			.and("subscriptions.id").ne(subscription.id()));

		Update update = new Update()
			.push("subscriptions").atPosition(0).each(subscription);

		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeSubscription(UUID userId, UUID interestId) {
		Query query = Query.query(Criteria.where("_id").is(userId));

		Update update = new Update()
			.pull("subscriptions", Query.query(Criteria.where("id").is(interestId)));

		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void addComment(UUID userId, CommentActivityDto comment) {
		Query query = Query.query(
			Criteria.where("_id").is(userId)
				.and("comments.id").ne(comment.id())
		);
		Update update = new Update()
			.push("comments").atPosition(0).value(comment);
		mongoTemplate.upsert(query, update, UserActivity.class);
	}

	@Override
	public void removeComment(UUID userId, UUID commentId) {
		Query query = Query.query(Criteria.where("_id").is(userId));
		Update update = new Update()
			.pull("comments", Query.query(Criteria.where("id").is(commentId)));
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void addCommentLike(UUID userId, CommentLikeActivityDto commentLike) {
		Query query = Query.query(
			Criteria.where("_id").is(userId)
				.and("commentLikes.id").ne(commentLike.id())
		);
		Update update = new Update()
			.push("commentLikes")
			.atPosition(0)
			.value(commentLike);
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void removeCommentLike(UUID userId, UUID commentLikeId) {
		Query query = Query.query(Criteria.where("_id").is(userId));
		Update update = new Update()
			.pull("commentLikes", Query.query(Criteria.where("id").is(commentLikeId)));
		mongoTemplate.updateFirst(query, update, UserActivity.class);
	}

	@Override
	public void addArticleView(UUID userId, ArticleViewDto articleView) {
		Query removeQuery =  Query.query(Criteria.where("_id").is(userId));
		Update removeUpdate = new Update()
			.pull("articleViews", Query.query(Criteria.where("id").is(articleView.id())));
		mongoTemplate.updateFirst(removeQuery, removeUpdate, UserActivity.class);

		Query addQuery = Query.query(Criteria.where("_id").is(userId));
		Update addUpdate = new Update()
			.push("articleViews").atPosition(0).value(articleView);
		mongoTemplate.upsert(addQuery, addUpdate, UserActivity.class);
	}
}
