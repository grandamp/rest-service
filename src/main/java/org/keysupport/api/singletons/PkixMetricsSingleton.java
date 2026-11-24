package org.keysupport.api.singletons;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Lock-free metrics collector tracking PKIX certificate path discovery vs.
 * certificate path validation timings and categorized indeterminate failures.
 */
public class PkixMetricsSingleton {

	public static final String METRIC_PATH_DISCOVERY = "vss.pkix.path.discovery";
	public static final String METRIC_PATH_VALIDATION = "vss.pkix.path.validation";
	public static final String METRIC_INDETERMINATE_FAILURES = "vss.pkix.indeterminate.failures";

	public static final String CATEGORY_NO_TRUST_PATH_EE = "no_trust_path_ee";
	public static final String CATEGORY_NO_TRUST_PATH_CA = "no_trust_path_ca";
	public static final String CATEGORY_SELF_ISSUED_ROLLOVER = "self_issued_rollover";
	public static final String CATEGORY_POLICY_CONSTRAINTS = "policy_constraints";
	public static final String CATEGORY_OTHER = "other";

	public static final List<String> KNOWN_INDETERMINATE_CATEGORIES = List.of(
			CATEGORY_NO_TRUST_PATH_EE,
			CATEGORY_NO_TRUST_PATH_CA,
			CATEGORY_SELF_ISSUED_ROLLOVER,
			CATEGORY_POLICY_CONSTRAINTS,
			CATEGORY_OTHER);

	private static final PkixMetricsSingleton INSTANCE = new PkixMetricsSingleton();

	private static final class BoundMeters {
		final MeterRegistry registry;
		final Timer discoverySuccess;
		final Timer discoveryFail;
		final Timer validationSuccess;
		final Timer validationFail;
		final Map<String, Counter> indeterminateCounters = new ConcurrentHashMap<>();

		BoundMeters(MeterRegistry registry) {
			this.registry = registry;
			this.discoverySuccess = buildTimer(registry, METRIC_PATH_DISCOVERY, "success",
					"Time spent in PKIX certificate path discovery (including CRL/OCSP revocation and AIA checks)");
			this.discoveryFail = buildTimer(registry, METRIC_PATH_DISCOVERY, "fail",
					"Time spent in PKIX certificate path discovery (including CRL/OCSP revocation and AIA checks)");
			this.validationSuccess = buildTimer(registry, METRIC_PATH_VALIDATION, "success",
					"Time spent in PKIX certificate path validation and response rendering");
			this.validationFail = buildTimer(registry, METRIC_PATH_VALIDATION, "fail",
					"Time spent in PKIX certificate path validation and response rendering");
			for (String category : KNOWN_INDETERMINATE_CATEGORIES) {
				counterFor(category);
			}
		}

		Counter counterFor(String category) {
			return indeterminateCounters.computeIfAbsent(category, cat -> Counter.builder(METRIC_INDETERMINATE_FAILURES)
					.tag("category", cat)
					.description("Live PKIX indeterminate validation failures (isAffirmativelyInvalid=false) by category")
					.register(registry));
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

	private final Map<MeterRegistry, BoundMeters> boundRegistries = new ConcurrentHashMap<>();

	private final LongAdder discoverySuccessCount = new LongAdder();
	private final LongAdder discoveryFailCount = new LongAdder();
	private final LongAdder discoverySuccessNanos = new LongAdder();
	private final LongAdder discoveryFailNanos = new LongAdder();

	private final LongAdder validationSuccessCount = new LongAdder();
	private final LongAdder validationFailCount = new LongAdder();
	private final LongAdder validationSuccessNanos = new LongAdder();
	private final LongAdder validationFailNanos = new LongAdder();

	private final Map<String, LongAdder> indeterminateByCategory = new ConcurrentHashMap<>();

	private PkixMetricsSingleton() {
		for (String category : KNOWN_INDETERMINATE_CATEGORIES) {
			indeterminateByCategory.put(category, new LongAdder());
		}
	}

	public static PkixMetricsSingleton getInstance() {
		return INSTANCE;
	}

	public void bindTo(MeterRegistry registry) {
		if (registry != null) {
			boundRegistries.computeIfAbsent(registry, BoundMeters::new);
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
		for (BoundMeters meters : boundRegistries.values()) {
			(success ? meters.discoverySuccess : meters.discoveryFail).record(nanos, TimeUnit.NANOSECONDS);
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
		for (BoundMeters meters : boundRegistries.values()) {
			(success ? meters.validationSuccess : meters.validationFail).record(nanos, TimeUnit.NANOSECONDS);
		}
	}

	public void recordIndeterminateFailure(String category) {
		String key = (null == category || category.isBlank()) ? CATEGORY_OTHER : category;
		indeterminateByCategory.computeIfAbsent(key, k -> new LongAdder()).increment();
		for (BoundMeters meters : boundRegistries.values()) {
			meters.counterFor(key).increment();
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

	public long getIndeterminateFailureCount(String category) {
		LongAdder adder = indeterminateByCategory.get(category);
		return null != adder ? adder.sum() : 0L;
	}

	public long getIndeterminateFailureTotal() {
		long total = 0L;
		for (LongAdder adder : indeterminateByCategory.values()) {
			total += adder.sum();
		}
		return total;
	}

	public Map<String, Long> getIndeterminateFailureBreakdown() {
		Map<String, Long> snapshot = new LinkedHashMap<>();
		for (String category : KNOWN_INDETERMINATE_CATEGORIES) {
			snapshot.put(category, getIndeterminateFailureCount(category));
		}
		for (Map.Entry<String, LongAdder> entry : indeterminateByCategory.entrySet()) {
			snapshot.putIfAbsent(entry.getKey(), entry.getValue().sum());
		}
		return Collections.unmodifiableMap(snapshot);
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
		indeterminateByCategory.values().forEach(LongAdder::reset);
	}
}
