package com.codeit.monew.article.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.article.domain.ArticleView;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID>, ArticleViewQueryRepository {
	boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);
}
