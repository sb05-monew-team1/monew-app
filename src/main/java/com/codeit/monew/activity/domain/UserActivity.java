package com.codeit.monew.activity.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.codeit.monew.article.dto.ArticleViewDto;
import com.codeit.monew.comment.dto.CommentActivityDto;
import com.codeit.monew.comment.dto.CommentLikeActivityDto;
import com.codeit.monew.interest.dto.SubscriptionDto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Document(collection = "user_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserActivity {
	@Id
	private UUID id;
	private String email;
	private String nickname;

	@CreatedDate
	private Instant createdAt;

	private List<SubscriptionDto> subscriptions;
	private List<CommentActivityDto> comments;
	private List<CommentLikeActivityDto> commentLikes;
	private List<ArticleViewDto> articleViews;

}
