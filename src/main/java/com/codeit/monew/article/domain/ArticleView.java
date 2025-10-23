package com.codeit.monew.article.domain;

import java.time.Instant;

import com.codeit.monew.common.base.BaseDomain;
import com.codeit.monew.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_views")
@NoArgsConstructor
@AllArgsConstructor
public class ArticleView extends BaseDomain {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false, updatable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id", nullable = false, updatable = false)
	private Article article;

	@Column(nullable = false)
	private Instant firstViewedAt;

	@Column(nullable = false)
	private Instant lastViewedAt;

}
