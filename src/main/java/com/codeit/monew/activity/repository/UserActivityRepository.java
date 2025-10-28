package com.codeit.monew.activity.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.codeit.monew.activity.domain.UserActivity;

public interface UserActivityRepository extends MongoRepository<UserActivity, UUID>, UserActivityQueryRepository {
}
