package org.keysupport.api.singletons;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.VssResponse;

/**
 * Bounded, short-TTL in-memory cache for VSS validation responses, keyed by
 * requestId (SHA-256 of x5t#S256 + ":" + validationPolicyId).
 */
public class ValidationCacheSingleton {

	public static final boolean DEFAULT_ENABLED = true;
	public static final long DEFAULT_TTL_SECONDS = 300L;
	public static final int DEFAULT_MAX_ENTRIES = 10_000;

	private static final class Holder {
		private static final ValidationCacheSingleton INSTANCE = new ValidationCacheSingleton();
	}

	private static final class CacheEntry {
		final VssResponse response;
		final long expiresAtMillis;

		CacheEntry(VssResponse response, long expiresAtMillis) {
			this.response = response;
			this.expiresAtMillis = expiresAtMillis;
		}
	}

	private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
	private final AtomicLong hits = new AtomicLong();
	private final AtomicLong misses = new AtomicLong();

	private volatile boolean enabled = DEFAULT_ENABLED;
	private volatile long ttlSeconds = DEFAULT_TTL_SECONDS;
	private volatile int maxEntries = DEFAULT_MAX_ENTRIES;

	private ValidationCacheSingleton() {
	}

	public static ValidationCacheSingleton getInstance() {
		return Holder.INSTANCE;
	}

	public void configure(boolean enabled, long ttlSeconds, int maxEntries) {
		this.enabled = enabled;
		this.ttlSeconds = Math.max(0L, ttlSeconds);
		this.maxEntries = Math.max(1, maxEntries);
		if (!enabled || this.ttlSeconds == 0L) {
			cache.clear();
		}
	}

	public VssResponse get(String requestId, long nowMillis) {
		if (!enabled || ttlSeconds <= 0L || null == requestId || requestId.isBlank()) {
			return null;
		}
		CacheEntry entry = cache.get(requestId);
		if (null == entry) {
			misses.incrementAndGet();
			return null;
		}
		if (nowMillis >= entry.expiresAtMillis) {
			cache.remove(requestId, entry);
			misses.incrementAndGet();
			return null;
		}
		hits.incrementAndGet();
		return entry.response;
	}

	public void put(String requestId, VssResponse response, Date certNotAfter, long nowMillis) {
		if (!enabled || ttlSeconds <= 0L || null == requestId || requestId.isBlank() || null == response) {
			return;
		}
		if (!(response.validationResult instanceof Success) && !(response.validationResult instanceof Fail)) {
			return;
		}
		long expiresAt = nowMillis + (ttlSeconds * 1000L);
		if (null != certNotAfter && certNotAfter.getTime() < expiresAt) {
			expiresAt = certNotAfter.getTime();
		}
		if (nowMillis >= expiresAt) {
			return;
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
		cache.put(requestId, new CacheEntry(response, expiresAt));
	}

	private void evictExpired(long nowMillis) {
		cache.entrySet().removeIf(e -> nowMillis >= e.getValue().expiresAtMillis);
	}

	public void clear() {
		cache.clear();
		hits.set(0L);
		misses.set(0L);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public long getTtlSeconds() {
		return ttlSeconds;
	}

	public int getMaxEntries() {
		return maxEntries;
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
