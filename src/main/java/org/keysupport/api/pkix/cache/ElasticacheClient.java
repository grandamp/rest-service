package org.keysupport.api.pkix.cache;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.MemcachedClient;

/**
 * This class uses a singleton pattern to manage our memcached/ElastiCache
 * client needs.
 * 
 * https://github.com/awslabs/aws-elasticache-cluster-client-memcached-for-java/blob/master/src/main/java/net/spy/memcached/MemcachedClient.java
 */

public class ElasticacheClient {

	private final static Logger LOG = LoggerFactory.getLogger(ElasticacheClient.class);

	private MemcachedClient mcClient = null;

	private String configEndpoint = null;

	private final static Integer clusterPort = 11211;

	private String name;

	public ElasticacheClient() {
		/*
		 * Memcached Start
		 */
		System.setProperty("net.spy.verifyAliveOnConnect", "true");
		/*
		 * Initialize memcached/Elasticache Config Address
		 */
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

	public Object getClient() {
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

	public void close() {
		mcClient.shutdown();
	}

	/*
	 * TODO: Add Memcached client methods for different Caching use-cases
	 */

}
