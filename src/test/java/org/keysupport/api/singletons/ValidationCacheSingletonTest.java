package org.keysupport.api.singletons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssResponse;

class ValidationCacheSingletonTest {

	private ValidationCacheSingleton cache;

	@BeforeEach
	void setUp() {
		cache = ValidationCacheSingleton.getInstance();
		cache.clear();
		cache.configure(true, 900L, 86_400L, 100);
	}

	@AfterEach
	void tearDown() {
		cache.clear();
		cache.configure(
				ValidationCacheSingleton.DEFAULT_ENABLED,
				ValidationCacheSingleton.DEFAULT_SUCCESS_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_AFFIRMATIVELY_INVALID_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_MAX_ENTRIES);
	}

	private static VssResponse successResponse(String id) {
		VssResponse r = new VssResponse();
		r.requestId = id;
		r.validationResult = new Success();
		return r;
	}

	private static VssResponse failResponse(String id, Boolean affirmativelyInvalid) {
		VssResponse r = new VssResponse();
		r.requestId = id;
		Fail f = new Fail();
		f.isAffirmativelyInvalid = affirmativelyInvalid;
		f.invalidityReasonText = "invalid";
		r.validationResult = f;
		return r;
	}

	@Test
	void cachesSuccessFor15MinAndAffirmativelyInvalidFailLongTerm() {
		long t0 = 1_000_000L;
		assertNull(cache.get("req-ok", t0));
		assertEquals(1L, cache.getMissCount());

		VssResponse ok = successResponse("req-ok");
		VssResponse affInvalid = failResponse("req-fail-aff", Boolean.TRUE);
		VssResponse nonAffInvalid = failResponse("req-fail-non", Boolean.FALSE);
		VssResponse nullAffInvalid = failResponse("req-fail-null", null);

		cache.put("req-ok", ok, new Date(t0 + 2_000_000L), t0);
		// Expired cert with isAffirmativelyInvalid=true is still cached long-term
		cache.put("req-fail-aff", affInvalid, new Date(t0 - 10_000L), t0);
		// Indeterminate failures (isAffirmativelyInvalid=false/null) are NOT cached
		cache.put("req-fail-non", nonAffInvalid, null, t0);
		cache.put("req-fail-null", nullAffInvalid, null, t0);

		assertEquals(2, cache.size());
		assertSame(ok, cache.get("req-ok", t0 + 899_999L));
		assertNull(cache.get("req-ok", t0 + 900_000L));

		assertSame(affInvalid, cache.get("req-fail-aff", t0 + 86_399_999L));
		assertNull(cache.get("req-fail-aff", t0 + 86_400_000L));
	}

	@Test
	void clampsSuccessTtlToCertNotAfter() {
		long t0 = 2_000_000L;
		VssResponse shortCert = successResponse("req-notafter");
		cache.put("req-notafter", shortCert, new Date(t0 + 5_000L), t0);
		assertNotNull(cache.get("req-notafter", t0 + 4_999L));
		assertNull(cache.get("req-notafter", t0 + 5_000L));
	}

	@Test
	void enforcesMaxEntriesAndDisabledState() {
		cache.configure(true, 900L, 86_400L, 2);
		long t0 = 3_000_000L;
		cache.put("a", successResponse("a"), null, t0);
		cache.put("b", successResponse("b"), null, t0);
		cache.put("c", successResponse("c"), null, t0);
		assertEquals(2, cache.size());

		VssResponse unknown = new VssResponse();
		unknown.validationResult = new ValidationResult() {};
		cache.put("unknown", unknown, null, t0);
		assertEquals(2, cache.size());

		cache.configure(false, 900L, 86_400L, 100);
		assertFalse(cache.isEnabled());
		assertEquals(0, cache.size());
		cache.put("d", successResponse("d"), null, t0);
		assertNull(cache.get("d", t0));

		cache.configure(true, 60L, 3600L, 50, false);
		assertTrue(cache.isEnabled());
		assertEquals(60L, cache.getSuccessTtlSeconds());
		assertEquals(3600L, cache.getAffirmativelyInvalidTtlSeconds());
		assertEquals(50, cache.getMaxEntries());
		assertFalse(cache.isCompactAuditLogging());
	}

	@Test
	void preSerializesJsonAndCoalescesConcurrentMisses() throws Exception {
		long t0 = 4_000_000L;
		AtomicInteger loaderCalls = new AtomicInteger();
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);

		ExecutorService pool = Executors.newFixedThreadPool(4);
		try {
			@SuppressWarnings("unchecked")
			Future<ValidationCacheSingleton.CacheEntry>[] futures = new Future[4];
			for (int i = 0; i < 4; i++) {
				futures[i] = pool.submit(() -> cache.executeSingleFlight("req-sf", () -> {
					loaderCalls.incrementAndGet();
					entered.countDown();
					try {
						release.await(5, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					VssResponse ok = successResponse("req-sf");
					return cache.put("req-sf", ok, null, new Date(t0 + 60_000L), t0);
				}));
			}
			assertTrue(entered.await(5, TimeUnit.SECONDS));
			Thread.sleep(50);
			release.countDown();
			for (Future<ValidationCacheSingleton.CacheEntry> f : futures) {
				ValidationCacheSingleton.CacheEntry entry = f.get(5, TimeUnit.SECONDS);
				assertNotNull(entry.serializedJson);
				assertTrue(entry.serializedJson.contains("\"SUCCESS\""));
			}
			assertEquals(1, loaderCalls.get());
		} finally {
			pool.shutdownNow();
		}
	}
}
