package com.codeit.monew.article.domain;

import com.codeit.monew.common.base.BaseUpdatableDomain;
import com.codeit.monew.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_views")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleView extends BaseUpdatableDomain {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false, updatable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "article_id", nullable = false, updatable = false)
	private Article article;

}
