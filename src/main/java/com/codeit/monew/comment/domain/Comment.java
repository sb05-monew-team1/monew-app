package com.codeit.monew.comment.domain;

import java.time.Instant;

import com.codeit.monew.article.domain.Article;
import com.codeit.monew.common.base.BaseUpdatableDomain;
import com.codeit.monew.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "comments")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseUpdatableDomain {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id")
	private Article article;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(nullable = false)
	private Long likeCount;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	/**
	 * 댓글 내용 수정
	 * @param content 새로운 댓글 내용 (1-500자)
	 */
	public void updateContent(String content) {
		this.content = content;
	}

	/**
	 * 논리 삭제 처리
	 */
	public void softDelete() {
		this.deletedAt = Instant.now();
	}

	/**
	 * 좋아요 수 증가
	 */
	public void increaseLikeCount() {
		this.likeCount++;
	}

	/**
	 * 좋아요 수 감소
	 */
	public void decreaseLikeCount() {
		if (this.likeCount > 0) {
			this.likeCount--;
		}
	}
}
