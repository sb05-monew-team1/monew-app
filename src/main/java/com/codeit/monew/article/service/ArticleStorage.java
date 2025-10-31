package com.codeit.monew.article.service;

import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.codeit.monew.common.config.AwsProperties;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Slf4j
@Component
public class ArticleStorage {
	private static final String PREFIX = "backup/";
	private static final String EXTENSION = ".jsonl";

	private final S3Client s3Client;
	private final String bucket;

	public ArticleStorage(AwsProperties props) {
		AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
		this.s3Client = S3Client.builder()
			.credentialsProvider(awsCredentialsProvider)
			.region(Region.of(props.region()))
			.build();
		this.bucket = props.bucket();
	}

	private String keyOf(String date) {
		return PREFIX + date + "-articles" + EXTENSION;
	}

	public InputStream get(String date) {
		String key = keyOf(date);
		GetObjectRequest request = GetObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();
		return s3Client.getObject(request);
	}
}
