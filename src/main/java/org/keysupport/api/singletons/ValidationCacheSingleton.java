package org.keysupport.api.singletons;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.VssResponse;

/**
 * Bounded, policy-aware in-memory cache and single-flight coalescer for VSS
 * validation responses, keyed by requestId (SHA-256 of x5t#S256 + ":" +
 * validationPolicyId).
 *
 * <ul>
 *   <li>{@link Success} responses are cached for {@code successTtlSeconds} (default 15 minutes), clamped to {@code cert.getNotAfter()}.</li>
 *   <li>{@link Fail} responses are cached for {@code affirmativelyInvalidTtlSeconds} (default 24 hours) only when {@code isAffirmativelyInvalid == true}.</li>
 *   <li>Pre-serializes JSON alongside {@link VssResponse} so cache hits bypass Jackson reflection.</li>
 *   <li>Coalesces concurrent cache misses per {@code requestId} so only one upstream PKIX/OCSP/CRL lookup runs at a time.</li>
 * </ul>
 */
public class ValidationCacheSingleton {

	public static final boolean DEFAULT_ENABLED = true;
	public static final long DEFAULT_SUCCESS_TTL_SECONDS = 900L;
	public static final long DEFAULT_AFFIRMATIVELY_INVALID_TTL_SECONDS = 86_400L;
	public static final int DEFAULT_MAX_ENTRIES = 10_000;
	public static final boolean DEFAULT_COMPACT_AUDIT_LOGGING = true;

	private static final class Holder {
		private static final ValidationCacheSingleton INSTANCE = new ValidationCacheSingleton();
	}

	public static final class CacheEntry {
		public final VssResponse response;
		public final String serializedJson;
		final long expiresAtMillis;

		CacheEntry(VssResponse response, String serializedJson, long expiresAtMillis) {
			this.response = response;
			this.serializedJson = serializedJson;
			this.expiresAtMillis = expiresAtMillis;
		}
	}

	private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, CompletableFuture<CacheEntry>> inFlight = new ConcurrentHashMap<>();
	private final AtomicLong hits = new AtomicLong();
	private final AtomicLong misses = new AtomicLong();

	private volatile boolean enabled = DEFAULT_ENABLED;
	private volatile long successTtlSeconds = DEFAULT_SUCCESS_TTL_SECONDS;
	private volatile long affirmativelyInvalidTtlSeconds = DEFAULT_AFFIRMATIVELY_INVALID_TTL_SECONDS;
	private volatile int maxEntries = DEFAULT_MAX_ENTRIES;
	private volatile boolean compactAuditLogging = DEFAULT_COMPACT_AUDIT_LOGGING;

	private ValidationCacheSingleton() {
	}

	public static ValidationCacheSingleton getInstance() {
		return Holder.INSTANCE;
	}

	public void configure(boolean enabled, long successTtlSeconds, long affirmativelyInvalidTtlSeconds, int maxEntries) {
		configure(enabled, successTtlSeconds, affirmativelyInvalidTtlSeconds, maxEntries, DEFAULT_COMPACT_AUDIT_LOGGING);
	}

	public void configure(boolean enabled, long successTtlSeconds, long affirmativelyInvalidTtlSeconds, int maxEntries,
			boolean compactAuditLogging) {
		this.enabled = enabled;
		this.successTtlSeconds = Math.max(0L, successTtlSeconds);
		this.affirmativelyInvalidTtlSeconds = Math.max(0L, affirmativelyInvalidTtlSeconds);
		this.maxEntries = Math.max(1, maxEntries);
		this.compactAuditLogging = compactAuditLogging;
		if (!enabled || (this.successTtlSeconds == 0L && this.affirmativelyInvalidTtlSeconds == 0L)) {
			cache.clear();
		}
	}

	public VssResponse get(String requestId, long nowMillis) {
		CacheEntry entry = getEntry(requestId, nowMillis);
		return null != entry ? entry.response : null;
	}

	public CacheEntry getEntry(String requestId, long nowMillis) {
		return lookupEntry(requestId, nowMillis, true);
	}

	/**
	 * Checks the cache without recording hit or miss metrics (for example, the
	 * double-checked re-read inside {@link #executeSingleFlight}).
	 */
	public CacheEntry peekEntry(String requestId, long nowMillis) {
		return lookupEntry(requestId, nowMillis, false);
	}

	private CacheEntry lookupEntry(String requestId, long nowMillis, boolean recordMetrics) {
		if (!enabled || null == requestId || requestId.isBlank()) {
			return null;
		}
		CacheEntry entry = cache.get(requestId);
		if (null == entry) {
			if (recordMetrics) {
				misses.incrementAndGet();
			}
			return null;
		}
		if (nowMillis >= entry.expiresAtMillis) {
			cache.remove(requestId, entry);
			if (recordMetrics) {
				misses.incrementAndGet();
			}
			return null;
		}
		if (recordMetrics) {
			hits.incrementAndGet();
		}
		return entry;
	}

	public void put(String requestId, VssResponse response, Date certNotAfter, long nowMillis) {
		put(requestId, response, null != response ? LoggingUtil.pojoToJson(response) : null, certNotAfter, nowMillis);
	}

	public CacheEntry put(String requestId, VssResponse response, String serializedJson, Date certNotAfter, long nowMillis) {
		if (null == response) {
			return null;
		}
		String json = null != serializedJson ? serializedJson : LoggingUtil.pojoToJson(response);
		CacheEntry created = new CacheEntry(response, json, nowMillis);
		if (!enabled || null == requestId || requestId.isBlank()) {
			return created;
		}
		long expiresAt;
		if (response.validationResult instanceof Success) {
			if (successTtlSeconds <= 0L) {
				return created;
			}
			expiresAt = nowMillis + (successTtlSeconds * 1000L);
			if (null != certNotAfter && certNotAfter.getTime() < expiresAt) {
				expiresAt = certNotAfter.getTime();
			}
			if (nowMillis >= expiresAt) {
				return created;
			}
		} else if (response.validationResult instanceof Fail fail) {
			if (!Boolean.TRUE.equals(fail.isAffirmativelyInvalid) || affirmativelyInvalidTtlSeconds <= 0L) {
				return created;
			}
			expiresAt = nowMillis + (affirmativelyInvalidTtlSeconds * 1000L);
		} else {
			return created;
		}

		if (cache.size() >= maxEntries && !cache.containsKey(requestId)) {
			evictExpired(nowMillis);
			if (cache.size() >= maxEntries) {
				Iterator<Map.Entry<String, CacheEntry>> it = cache.entrySet().iterator();
				if (it.hasNext()) {
					it.next();
					it.remove();
				}
			}
		}
		CacheEntry stored = new CacheEntry(response, json, expiresAt);
		cache.put(requestId, stored);
		return stored;
	}

	/**
	 * Coalesces concurrent cache misses for the same {@code requestId} when caching
	 * is enabled, running {@code loader} at most once per concurrent wave.
	 */
	public CacheEntry executeSingleFlight(String requestId, Supplier<CacheEntry> loader) {
		if (!enabled || null == requestId || requestId.isBlank()) {
			return loader.get();
		}
		CompletableFuture<CacheEntry> newFuture = new CompletableFuture<>();
		CompletableFuture<CacheEntry> existing = inFlight.putIfAbsent(requestId, newFuture);
		if (null == existing) {
			try {
				CacheEntry result = loader.get();
				newFuture.complete(result);
				return result;
			} catch (Throwable t) {
				newFuture.completeExceptionally(t);
				throw t;
			} finally {
				inFlight.remove(requestId, newFuture);
			}
		}
		try {
			return existing.join();
		} catch (CompletionException ce) {
			Throwable cause = ce.getCause();
			if (cause instanceof RuntimeException re) {
				throw re;
			}
			if (cause instanceof Error err) {
				throw err;
			}
			throw ce;
		}
	}

	private void evictExpired(long nowMillis) {
		cache.entrySet().removeIf(e -> nowMillis >= e.getValue().expiresAtMillis);
	}

	public void clear() {
		cache.clear();
		inFlight.clear();
		hits.set(0L);
		misses.set(0L);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public long getSuccessTtlSeconds() {
		return successTtlSeconds;
	}

	public long getAffirmativelyInvalidTtlSeconds() {
		return affirmativelyInvalidTtlSeconds;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public boolean isCompactAuditLogging() {
		return compactAuditLogging;
	}

	public int size() {
		return cache.size();
	}

	public long getHitCount() {
		return hits.get();
	}

	public long getMissCount() {
		return misses.get();
	}
}
