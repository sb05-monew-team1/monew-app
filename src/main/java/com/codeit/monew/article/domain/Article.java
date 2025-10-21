package com.codeit.monew.article.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "articles")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Article {

	// id            uuid PRIMARY KEY,
	// source        varchar(20)  NOT NULL,
	// source_url    varchar(500) NOT NULL UNIQUE,
	// title         varchar(500) NOT NULL,
	// publish_date  timestamptz  NOT NULL,
	// summary       varchar(500),
	// comment_count bigint       NOT NULL DEFAULT 0,
	// view_count    bigint       NOT NULL DEFAULT 0,
	// collected_at  timestamptz  NOT NULL DEFAULT now(),
	// created_at    timestamptz  NOT NULL DEFAULT now(),
	// updated_at    timestamptz  NOT NULL DEFAULT now(),
	// deleted_at    timestamptz

	@Id
	private UUID id;

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

	@Column(nullable = false)
	private Instant collected_at;

	@Column(nullable = false)
	@CreatedDate
	private Instant created_at;

	@Column(nullable = false)
	@LastModifiedDate
	private Instant updated_at;

	private Instant deleted_at;

}
