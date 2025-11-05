package com.codeit.monew.common.config;

import java.time.Duration;

import io.micrometer.cloudwatch2.CloudWatchConfig;

class CloudWatchMetricsConfigAdapter implements CloudWatchConfig {

	private final CloudWatchMetricsProperties properties;

	CloudWatchMetricsConfigAdapter(CloudWatchMetricsProperties properties) {
		this.properties = properties;
	}

	@Override
	public String namespace() {
		return properties.getNamespace();
	}

	@Override
	public Duration step() {
		Duration step = properties.getStep();
		return step != null ? step : CloudWatchConfig.super.step();
	}

	@Override
	public int batchSize() {
		Integer batchSize = properties.getBatchSize();
		return batchSize != null ? batchSize : CloudWatchConfig.super.batchSize();
	}

	@Override
	public String get(String key) {
		return null;
	}
}
