package org.keysupport.api.singletons;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.MemcachedClient;

/**
 * This class uses a singleton pattern to manage our memcached/ElastiCache
 * client needs.
 */

public class ElasticacheClientSingleton {

	private final static Logger LOG = LoggerFactory.getLogger(ElasticacheClientSingleton.class);

	private MemcachedClient mcClient = null;

	private static String configEndpoint = null;

	private final static Integer clusterPort = 11211;

	private String name;

	private ElasticacheClientSingleton() {
		/*
		 * Memcached Start
		 */
		System.setProperty("net.spy.verifyAliveOnConnect", "true");
		configEndpoint = System.getenv("MEMCACHED_CNF");
		/*
		 * temp for testing
		 */
		//configEndpoint = "127.0.0.1";
		if (null == configEndpoint) {
			LOG.error("MEMCACHED_CNF Not Set!");
		}
		try {
			mcClient = new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
		} catch (IOException e) {
			LOG.error("Error creating memcached client", e);
		}
	}

	private static class SingletonHelper {
		private static final ElasticacheClientSingleton INSTANCE = new ElasticacheClientSingleton();
	}

	public static ElasticacheClientSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public Object getNativeCache() {
		return mcClient;
	}

	public String getName() {
		return name;
	}

	public void clear() {
		mcClient.flush();
		LOG.info("Cache Fluch complete");
	}

	public void evict(final String key) {
		mcClient.delete(key.toString());
		LOG.info("Cache Delete for key: " + key);
	}

	public byte[] get(final String key) {
		byte[] value = null;
		try {
			value = (byte[]) mcClient.get(key);
		} catch (final Exception e) {
			LOG.error(e.getMessage());
		}
		if (value == null) {
			LOG.info("Cache Miss for key: " + key);
			return null;
		}
		LOG.info("Cache Hit for key: " + key);
		return value;
	}

	public void put(final String key, final byte[] value) {
		putWithTtl(key, 3600, value);
	}

	public void putWithTtl(final String key, final int ttl, final byte[] value) {
		if (value != null) {
			mcClient.set(key.toString(), ttl, value);
			LOG.info("Cache Put for key: " + key);
		}
	}

	/*
	 * TODO: Add Memcached client methods for different Caching use-cases
	 */

}
