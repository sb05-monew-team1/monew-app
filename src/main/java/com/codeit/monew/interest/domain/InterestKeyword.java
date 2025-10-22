package com.codeit.monew.interest.domain;

import com.codeit.monew.common.base.BaseDomain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(name = "interest_keywords")
@AllArgsConstructor
@NoArgsConstructor
public class InterestKeyword extends BaseDomain {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "interest_id")
	private Interest interest;

	@Column(name = "keyword")
	private String keyword;
}
