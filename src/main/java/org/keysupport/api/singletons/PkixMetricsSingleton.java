package org.keysupport.api.singletons;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Lock-free metrics collector tracking PKIX certificate path discovery vs.
 * certificate path validation timings.
 */
public class PkixMetricsSingleton {

	public static final String METRIC_PATH_DISCOVERY = "vss.pkix.path.discovery";
	public static final String METRIC_PATH_VALIDATION = "vss.pkix.path.validation";

	private static final PkixMetricsSingleton INSTANCE = new PkixMetricsSingleton();

	private static final class BoundTimers {
		final Timer discoverySuccess;
		final Timer discoveryFail;
		final Timer validationSuccess;
		final Timer validationFail;

		BoundTimers(MeterRegistry registry) {
			this.discoverySuccess = buildTimer(registry, METRIC_PATH_DISCOVERY, "success",
					"Time spent in PKIX certificate path discovery (including CRL/OCSP revocation and AIA checks)");
			this.discoveryFail = buildTimer(registry, METRIC_PATH_DISCOVERY, "fail",
					"Time spent in PKIX certificate path discovery (including CRL/OCSP revocation and AIA checks)");
			this.validationSuccess = buildTimer(registry, METRIC_PATH_VALIDATION, "success",
					"Time spent in PKIX certificate path validation and response rendering");
			this.validationFail = buildTimer(registry, METRIC_PATH_VALIDATION, "fail",
					"Time spent in PKIX certificate path validation and response rendering");
		}

		private static Timer buildTimer(MeterRegistry registry, String name, String outcome, String description) {
			return Timer.builder(name)
					.tag("outcome", outcome)
					.description(description)
					.publishPercentiles(0.5, 0.95, 0.99)
					.distributionStatisticExpiry(Duration.ofMinutes(5))
					.register(registry);
		}
	}

	private final Map<MeterRegistry, BoundTimers> boundRegistries = new ConcurrentHashMap<>();

	private final LongAdder discoverySuccessCount = new LongAdder();
	private final LongAdder discoveryFailCount = new LongAdder();
	private final LongAdder discoverySuccessNanos = new LongAdder();
	private final LongAdder discoveryFailNanos = new LongAdder();

	private final LongAdder validationSuccessCount = new LongAdder();
	private final LongAdder validationFailCount = new LongAdder();
	private final LongAdder validationSuccessNanos = new LongAdder();
	private final LongAdder validationFailNanos = new LongAdder();

	private PkixMetricsSingleton() {
	}

	public static PkixMetricsSingleton getInstance() {
		return INSTANCE;
	}

	public void bindTo(MeterRegistry registry) {
		if (registry != null) {
			boundRegistries.computeIfAbsent(registry, BoundTimers::new);
		}
	}

	public void unbind(MeterRegistry registry) {
		if (registry != null) {
			boundRegistries.remove(registry);
		}
	}

	public void recordDiscovery(long durationNanos, boolean success) {
		long nanos = Math.max(0L, durationNanos);
		if (success) {
			discoverySuccessCount.increment();
			discoverySuccessNanos.add(nanos);
		} else {
			discoveryFailCount.increment();
			discoveryFailNanos.add(nanos);
		}
		for (BoundTimers timers : boundRegistries.values()) {
			(success ? timers.discoverySuccess : timers.discoveryFail).record(nanos, TimeUnit.NANOSECONDS);
		}
	}

	public void recordValidation(long durationNanos, boolean success) {
		long nanos = Math.max(0L, durationNanos);
		if (success) {
			validationSuccessCount.increment();
			validationSuccessNanos.add(nanos);
		} else {
			validationFailCount.increment();
			validationFailNanos.add(nanos);
		}
		for (BoundTimers timers : boundRegistries.values()) {
			(success ? timers.validationSuccess : timers.validationFail).record(nanos, TimeUnit.NANOSECONDS);
		}
	}

	public long getDiscoverySuccessCount() {
		return discoverySuccessCount.sum();
	}

	public long getDiscoveryFailCount() {
		return discoveryFailCount.sum();
	}

	public long getDiscoveryCount() {
		return discoverySuccessCount.sum() + discoveryFailCount.sum();
	}

	public long getDiscoveryTotalNanos() {
		return discoverySuccessNanos.sum() + discoveryFailNanos.sum();
	}

	public long getValidationSuccessCount() {
		return validationSuccessCount.sum();
	}

	public long getValidationFailCount() {
		return validationFailCount.sum();
	}

	public long getValidationCount() {
		return validationSuccessCount.sum() + validationFailCount.sum();
	}

	public long getValidationTotalNanos() {
		return validationSuccessNanos.sum() + validationFailNanos.sum();
	}

	public void resetCountersForTesting() {
		discoverySuccessCount.reset();
		discoveryFailCount.reset();
		discoverySuccessNanos.reset();
		discoveryFailNanos.reset();
		validationSuccessCount.reset();
		validationFailCount.reset();
		validationSuccessNanos.reset();
		validationFailNanos.reset();
	}
}
