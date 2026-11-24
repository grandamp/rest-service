package org.keysupport.api.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.cert.CertStore;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.PkixMetricsSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.mockito.MockedStatic;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class VssHealthAndMetricsTest {

	@Test
	void validationPoliciesHealthIndicator_reportsDownWhenEmptyAndUpWhenLoaded() {
		ValidationPoliciesHealthIndicator indicator = new ValidationPoliciesHealthIndicator();
		ValidationPoliciesSingleton mockSingleton = mock(ValidationPoliciesSingleton.class);

		try (MockedStatic<ValidationPoliciesSingleton> mocked = mockStatic(ValidationPoliciesSingleton.class)) {
			mocked.when(ValidationPoliciesSingleton::getInstance).thenReturn(mockSingleton);

			when(mockSingleton.getValidationPolicies()).thenReturn(null);
			Health down = indicator.health();
			assertEquals(Status.DOWN, down.getStatus());

			ValidationPolicies policies = new ValidationPolicies();
			policies.validationPolicies = List.of(new ValidationPolicy());
			when(mockSingleton.getValidationPolicies()).thenReturn(policies);
			Health up = indicator.health();
			assertEquals(Status.UP, up.getStatus());
			assertEquals(1, up.getDetails().get("policyCount"));
		}
	}

	@Test
	void intermediateCacheHealthIndicator_reportsDownWhenEmptyAndUpWhenPopulated() {
		IntermediateCacheHealthIndicator indicator = new IntermediateCacheHealthIndicator();
		IntermediateCacheSingleton mockSingleton = mock(IntermediateCacheSingleton.class);
		CertStore mockStore = mock(CertStore.class);

		try (MockedStatic<IntermediateCacheSingleton> mocked = mockStatic(IntermediateCacheSingleton.class)) {
			mocked.when(IntermediateCacheSingleton::getInstance).thenReturn(mockSingleton);

			when(mockSingleton.getIntermediates()).thenReturn(null);
			when(mockSingleton.getCachedIntermediateCount()).thenReturn(0);
			assertEquals(Status.DOWN, indicator.health().getStatus());

			when(mockSingleton.getIntermediates()).thenReturn(mockStore);
			when(mockSingleton.getCachedIntermediateCount()).thenReturn(141);
			Health up = indicator.health();
			assertEquals(Status.UP, up.getStatus());
			assertEquals(141, up.getDetails().get("intermediateCount"));
		}
	}

	@Test
	void vssMetricsConfiguration_registersAllMeters() {
		VssMetricsConfiguration config = new VssMetricsConfiguration();
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		config.vssMeterBinder().bindTo(registry);

		assertNotNull(registry.find("vss.validation.cache.entries").gauge());
		assertNotNull(registry.find("vss.validation.cache.hits").functionCounter());
		assertNotNull(registry.find("vss.validation.cache.misses").functionCounter());
		assertNotNull(registry.find("vss.intermediates.cached").gauge());
		assertNotNull(registry.find("vss.policies.loaded").gauge());
		assertNotNull(registry.find(PkixMetricsSingleton.METRIC_PATH_DISCOVERY).tag("outcome", "success").timer());
		assertNotNull(registry.find(PkixMetricsSingleton.METRIC_PATH_DISCOVERY).tag("outcome", "fail").timer());
		assertNotNull(registry.find(PkixMetricsSingleton.METRIC_PATH_VALIDATION).tag("outcome", "success").timer());
		assertNotNull(registry.find(PkixMetricsSingleton.METRIC_PATH_VALIDATION).tag("outcome", "fail").timer());
		for (String cat : PkixMetricsSingleton.KNOWN_INDETERMINATE_CATEGORIES) {
			assertNotNull(registry.find(PkixMetricsSingleton.METRIC_INDETERMINATE_FAILURES).tag("category", cat).counter());
		}

		PkixMetricsSingleton metrics = PkixMetricsSingleton.getInstance();
		try {
			metrics.recordDiscovery(12_000_000L, true);
			metrics.recordDiscovery(5_000_000L, false);
			metrics.recordValidation(3_000_000L, true);
			metrics.recordValidation(1_000_000L, false);
			metrics.recordIndeterminateFailure(PkixMetricsSingleton.CATEGORY_NO_TRUST_PATH_EE);
			metrics.recordIndeterminateFailure(PkixMetricsSingleton.CATEGORY_NO_TRUST_PATH_CA);

			assertEquals(1L, registry.find(PkixMetricsSingleton.METRIC_PATH_DISCOVERY).tag("outcome", "success").timer().count());
			assertEquals(1L, registry.find(PkixMetricsSingleton.METRIC_PATH_DISCOVERY).tag("outcome", "fail").timer().count());
			assertEquals(1L, registry.find(PkixMetricsSingleton.METRIC_PATH_VALIDATION).tag("outcome", "success").timer().count());
			assertEquals(1L, registry.find(PkixMetricsSingleton.METRIC_PATH_VALIDATION).tag("outcome", "fail").timer().count());
			assertEquals(1.0,
					registry.find(PkixMetricsSingleton.METRIC_INDETERMINATE_FAILURES)
							.tag("category", PkixMetricsSingleton.CATEGORY_NO_TRUST_PATH_EE).counter().count());
			assertEquals(1.0,
					registry.find(PkixMetricsSingleton.METRIC_INDETERMINATE_FAILURES)
							.tag("category", PkixMetricsSingleton.CATEGORY_NO_TRUST_PATH_CA).counter().count());
		} finally {
			metrics.unbind(registry);
		}
	}
}
