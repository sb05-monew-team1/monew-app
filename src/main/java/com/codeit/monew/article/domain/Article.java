package com.codeit.monew.article.domain;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.codeit.monew.common.base.BaseUpdatableDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "articles")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Article extends BaseUpdatableDomain {

	@Column(nullable = false, length = 20)
	private String source;

	@Column(nullable = false, unique = true, length = 500)
	private String source_url;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(nullable = false)
	private Instant publish_date;

	@Column(length = 500)
	private String summary;

	@Column(nullable = false)
	private long comment_count;

	@Column(nullable = false)
	private long view_count;

	private Instant deleted_at;
}
