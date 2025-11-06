package com.codeit.monew.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;

class CloudWatchMetricsConfigTest {

	private final CloudWatchMetricsConfig config = new CloudWatchMetricsConfig();

	@Test
	void cloudWatchConfigReturnsAdapter() {
		CloudWatchMetricsProperties properties = new CloudWatchMetricsProperties();

		CloudWatchConfig result = config.cloudWatchConfig(properties);

		assertThat(result).isInstanceOf(CloudWatchMetricsConfigAdapter.class);
	}

	@Test
	void cloudWatchAsyncClientUsesProvidedRegion() {
		CloudWatchMetricsProperties properties = new CloudWatchMetricsProperties();
		properties.setRegion("ap-northeast-2");

		try (MockedStatic<CloudWatchAsyncClient> mockedStatic = mockStatic(CloudWatchAsyncClient.class)) {
			CloudWatchAsyncClientBuilder builder = mock(CloudWatchAsyncClientBuilder.class);
			CloudWatchAsyncClient client = mock(CloudWatchAsyncClient.class);

			mockedStatic.when(CloudWatchAsyncClient::builder).thenReturn(builder);
			when(builder.region(any(Region.class))).thenReturn(builder);
			when(builder.build()).thenReturn(client);

			CloudWatchAsyncClient result = config.cloudWatchAsyncClient(properties);

			assertThat(result).isSameAs(client);
			verify(builder).region(Region.of("ap-northeast-2"));
			verify(builder).build();
		}
	}

	@Test
	void cloudWatchAsyncClientSkipsRegionWhenNotProvided() {
		CloudWatchMetricsProperties properties = new CloudWatchMetricsProperties();
		properties.setRegion("  ");

		try (MockedStatic<CloudWatchAsyncClient> mockedStatic = mockStatic(CloudWatchAsyncClient.class)) {
			CloudWatchAsyncClientBuilder builder = mock(CloudWatchAsyncClientBuilder.class);
			CloudWatchAsyncClient client = mock(CloudWatchAsyncClient.class);

			mockedStatic.when(CloudWatchAsyncClient::builder).thenReturn(builder);
			when(builder.build()).thenReturn(client);

			CloudWatchAsyncClient result = config.cloudWatchAsyncClient(properties);

			assertThat(result).isSameAs(client);
			verify(builder, never()).region(any(Region.class));
			verify(builder).build();
		}
	}

	@Test
	void cloudWatchMeterRegistryCreatesRegistry() {
		CloudWatchMetricsProperties properties = new CloudWatchMetricsProperties();
		CloudWatchConfig cloudWatchConfig = config.cloudWatchConfig(properties);
		Clock clock = Clock.SYSTEM;
		CloudWatchAsyncClient client = mock(CloudWatchAsyncClient.class);

		CloudWatchMeterRegistry registry = config.cloudWatchMeterRegistry(cloudWatchConfig, clock, client);

		assertThat(registry).isNotNull();
		assertThat(registry.config()).isNotNull();
	}
}
