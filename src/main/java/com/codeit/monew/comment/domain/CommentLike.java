package com.codeit.monew.comment.domain;

import com.codeit.monew.common.base.BaseDomain;
import com.codeit.monew.common.base.BaseUpdatableDomain;
import com.codeit.monew.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 댓글 좋아요 엔티티
 */
@Entity
@Table(
	name = "comment_likes",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"comment_id", "user_id"})
	}
)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentLike extends BaseDomain {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
