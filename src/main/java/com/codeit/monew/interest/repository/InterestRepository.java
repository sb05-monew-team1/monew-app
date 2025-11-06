package com.codeit.monew.interest.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.codeit.monew.interest.domain.Interest;

public interface InterestRepository
	extends JpaRepository<Interest, UUID>, QuerydslPredicateExecutor<Interest>, InterestQueryRepository {
	@Query("SELECT i.name FROM Interest i")
	List<String> findAllNames();
}
