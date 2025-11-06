package com.codeit.monew.interest.repository;

import java.util.List;

import com.codeit.monew.interest.domain.Interest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

public interface InterestQueryRepository {

	List<Interest> findAllWithOrders(Predicate predicate, List<OrderSpecifier<?>> orderSpecifiers, int limit);
}
