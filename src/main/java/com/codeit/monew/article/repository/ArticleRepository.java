package com.codeit.monew.article.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.monew.article.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleQueryRepository {
}
