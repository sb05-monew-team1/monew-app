package com.codeit.monew.common.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.metrics.export.cloudwatch")
public class CloudWatchMetricsProperties {

	private boolean enabled = true;
	private String namespace = "application";
	private Duration step = Duration.ofMinutes(1);
	private Integer batchSize;
	private String region;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Duration getStep() {
		return step;
	}

	public void setStep(Duration step) {
		this.step = step;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
}
