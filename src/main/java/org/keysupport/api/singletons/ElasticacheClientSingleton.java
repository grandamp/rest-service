package org.keysupport.api.singletons;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.MemcachedClient;

/**
 * This class uses a singleton pattern to manage our memcached/ElastiCache client needs.
 */

public class ElasticacheClientSingleton {

	private final static Logger LOG = LoggerFactory.getLogger(ElasticacheClientSingleton.class);

	private MemcachedClient mcClient = null;

	private static String configEndpoint = null;

	private final static Integer clusterPort = 11211;

	private ElasticacheClientSingleton() {
		/*
		 * Memcached Start
		 */
		System.setProperty("net.spy.verifyAliveOnConnect", "true");
		configEndpoint = System.getenv("MEMCACHED_CNF");
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

	public MemcachedClient getClient() {
		return mcClient;
	}

	/*
	 * TODO: Add Memcached client methods for different Caching use-cases
	 */
}
