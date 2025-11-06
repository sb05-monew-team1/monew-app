package com.codeit.monew.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.micrometer.cloudwatch2.CloudWatchConfig;

class CloudWatchMetricsConfigAdapterTest {

	@Test
	void returnsValuesFromProperties() {
		CloudWatchMetricsProperties props = new CloudWatchMetricsProperties();
		props.setNamespace("monew");
		props.setStep(Duration.ofSeconds(30));
		props.setBatchSize(20);

		CloudWatchMetricsConfigAdapter adapter = new CloudWatchMetricsConfigAdapter(props);

		assertThat(adapter.namespace()).isEqualTo("monew");
		assertThat(adapter.step()).isEqualTo(Duration.ofSeconds(30));
		assertThat(adapter.batchSize()).isEqualTo(20);
		assertThat(adapter.get("any.key")).isNull();
	}

	@Test
	void fallsBackToDefaultsWhenPropertiesMissing() {
		CloudWatchMetricsProperties props = new CloudWatchMetricsProperties();
		props.setNamespace(null);
		props.setStep(null);
		props.setBatchSize(null);

		CloudWatchMetricsConfigAdapter adapter = new CloudWatchMetricsConfigAdapter(props);
		CloudWatchConfig defaults = key -> null;

		assertThat(adapter.namespace()).isNull();
		assertThat(adapter.step()).isEqualTo(defaults.step());
		assertThat(adapter.batchSize()).isEqualTo(defaults.batchSize());
	}
}
