package com.codeit.monew.article.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.codeit.monew.article.domain.Article;

/**
 * 기사 Repository
 */
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleQueryRepository {
	boolean existsBySourceUrl(String sourceUrl);

	@Query("select a.sourceUrl from Article a")
	Set<String> findAllSourceUrls();
}
