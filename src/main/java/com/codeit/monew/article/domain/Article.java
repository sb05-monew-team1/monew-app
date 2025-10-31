package com.codeit.monew.article.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.monew.comment.domain.Comment;
import com.codeit.monew.common.base.BaseDomain;
import com.codeit.monew.interest.domain.Interest;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "articles")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@Getter
public class Article extends BaseDomain {

	@Column(nullable = false, updatable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private ArticleSource source;

	@Column(nullable = false, unique = true, updatable = false, length = 500)
	private String sourceUrl;

	@Column(nullable = false, updatable = false, length = 500)
	private String title;

	@Column(nullable = false, updatable = false)
	private Instant publishDate;

	@Column(nullable = false, updatable = false)
	private Instant collectedAt;

	@Column(updatable = false, length = 500)
	private String summary;

	@Column(nullable = false)
	private long commentCount;

	@Column(nullable = false)
	private long viewCount;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "article", orphanRemoval = true, cascade = CascadeType.ALL)
	private List<Comment> comments;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "article", orphanRemoval = true, cascade = CascadeType.ALL)
	private List<ArticleView> articleViews;

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	@JoinTable(
		name = "article_interests",
		joinColumns = @JoinColumn(name = "article_id"),
		inverseJoinColumns = @JoinColumn(name = "interest_id")
	)
	private List<Interest> interests = new ArrayList<>();

	private Instant deleted_at;

	public void deleteSoft(Instant deletedAt) {
		if (deletedAt != null) {
			this.deleted_at = deletedAt;
		}
	}
}
