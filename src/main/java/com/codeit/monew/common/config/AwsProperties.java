package com.codeit.monew.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.storage.s3")
public record AwsProperties(
	String accessKey,
	String secretKey,
	String region,
	String bucket,
	Long expiration
) {
}
