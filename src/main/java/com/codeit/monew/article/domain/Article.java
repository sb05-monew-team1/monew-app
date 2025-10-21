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
