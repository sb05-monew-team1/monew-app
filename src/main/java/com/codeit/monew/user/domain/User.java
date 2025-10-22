package com.codeit.monew.user.domain;

import com.codeit.monew.common.base.BaseUpdatableDomain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseUpdatableDomain {
}
