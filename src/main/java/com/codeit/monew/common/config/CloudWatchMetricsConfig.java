package com.codeit.monew.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;

@Configuration
@ConditionalOnClass(CloudWatchMeterRegistry.class)
@EnableConfigurationProperties(CloudWatchMetricsProperties.class)
@ConditionalOnProperty(prefix = "management.metrics.export.cloudwatch", name = "enabled", havingValue = "true")
public class CloudWatchMetricsConfig {

	@Bean
	@ConditionalOnMissingBean
	CloudWatchConfig cloudWatchConfig(CloudWatchMetricsProperties properties) {
		return new CloudWatchMetricsConfigAdapter(properties);
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	CloudWatchAsyncClient cloudWatchAsyncClient(CloudWatchMetricsProperties properties) {
		CloudWatchAsyncClientBuilder builder = CloudWatchAsyncClient.builder();
		if (properties.getRegion() != null && !properties.getRegion().isBlank()) {
			builder = builder.region(Region.of(properties.getRegion()));
		}
		return builder.build();
	}

	@Bean
	@ConditionalOnMissingBean(CloudWatchMeterRegistry.class)
	CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchConfig config, Clock clock,
		CloudWatchAsyncClient cloudWatchAsyncClient) {
		return new CloudWatchMeterRegistry(config, clock, cloudWatchAsyncClient);
	}
}
