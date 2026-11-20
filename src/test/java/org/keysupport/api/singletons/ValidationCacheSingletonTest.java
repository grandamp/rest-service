package org.keysupport.api.singletons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

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
		cache.configure(true, 300L, 100);
	}

	@AfterEach
	void tearDown() {
		cache.clear();
		cache.configure(
				ValidationCacheSingleton.DEFAULT_ENABLED,
				ValidationCacheSingleton.DEFAULT_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_MAX_ENTRIES);
	}

	private static VssResponse successResponse(String id) {
		VssResponse r = new VssResponse();
		r.requestId = id;
		r.validationResult = new Success();
		return r;
	}

	private static VssResponse failResponse(String id) {
		VssResponse r = new VssResponse();
		r.requestId = id;
		Fail f = new Fail();
		f.invalidityReasonText = "revoked";
		r.validationResult = f;
		return r;
	}

	@Test
	void cachesSuccessAndFailAndTracksHitMissCounts() {
		long t0 = 1_000_000L;
		assertNull(cache.get("req-1", t0));
		assertEquals(1L, cache.getMissCount());
		assertEquals(0L, cache.getHitCount());

		VssResponse ok = successResponse("req-1");
		VssResponse fail = failResponse("req-2");
		cache.put("req-1", ok, new Date(t0 + 600_000L), t0);
		cache.put("req-2", fail, new Date(t0 + 600_000L), t0);
		assertEquals(2, cache.size());

		assertSame(ok, cache.get("req-1", t0 + 1_000L));
		assertSame(fail, cache.get("req-2", t0 + 1_000L));
		assertEquals(2L, cache.getHitCount());
	}

	@Test
	void expiresAfterTtlOrCertNotAfter() {
		long t0 = 2_000_000L;
		VssResponse ok = successResponse("req-ttl");
		cache.put("req-ttl", ok, new Date(t0 + 600_000L), t0);

		assertNotNull(cache.get("req-ttl", t0 + 299_999L));
		assertNull(cache.get("req-ttl", t0 + 300_000L));
		assertEquals(0, cache.size());

		VssResponse shortCert = successResponse("req-notafter");
		cache.put("req-notafter", shortCert, new Date(t0 + 5_000L), t0);
		assertNotNull(cache.get("req-notafter", t0 + 4_999L));
		assertNull(cache.get("req-notafter", t0 + 5_000L));
	}

	@Test
	void enforcesMaxEntriesAndDisabledState() {
		cache.configure(true, 300L, 2);
		long t0 = 3_000_000L;
		cache.put("a", successResponse("a"), null, t0);
		cache.put("b", successResponse("b"), null, t0);
		cache.put("c", successResponse("c"), null, t0);
		assertEquals(2, cache.size());

		// Non-Success/Fail validationResult is ignored
		VssResponse unknown = new VssResponse();
		unknown.validationResult = new ValidationResult() {};
		cache.put("unknown", unknown, null, t0);
		assertEquals(2, cache.size());

		// Disabling clears cache
		cache.configure(false, 300L, 100);
		assertFalse(cache.isEnabled());
		assertEquals(0, cache.size());
		cache.put("d", successResponse("d"), null, t0);
		assertNull(cache.get("d", t0));

		cache.configure(true, 60L, 50);
		assertTrue(cache.isEnabled());
		assertEquals(60L, cache.getTtlSeconds());
		assertEquals(50, cache.getMaxEntries());
	}
}
