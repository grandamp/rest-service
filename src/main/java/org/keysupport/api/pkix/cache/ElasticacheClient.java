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
 * 
 * TODO: We should consider a schemaVersion attribute, to potentially track the
 * cache during deployments, and allow a flush() during a deployment when
 * identified.
 * 
 * Any JSON schema change, or change in cache should consider selective
 * evictions, or a complete cache flush.
 * 
 * The schemaVersion attribute could simply be the SHA-256 digest of the OAS3 JSON for this implementation.
 * 
 * I.e., https://api.keysupport.org/v3/api-docs
 * 
 * We *should* be able to internally fetch this JSON.
 */

public class ElasticacheClient {

	public enum cache {
		HIT, MISS, PUT, DELETE, FLUSH
	};

	private final static Logger LOG = LoggerFactory.getLogger(ElasticacheClient.class);

	private MemcachedClient mcClient = null;

	private String configEndpoint = null;

	private final static Integer clusterPort = 11211;

	private String name;

	/**
	 * 15 Minute TTL for Valid Certificates
	 * 
	 * TODO: Consider TTL Override
	 */
	public final static int VALID_CERT_TTL = 900;

	/**
	 * 7 Day TTL for In-valid Certificates
	 * 
	 * TODO: Consider TTL Override
	 */
	public final static int INVALID_CERT_TTL = 604800;

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

	public void flush() {
		mcClient.flush();
		LOG.info("{\"cache\":{\"status\":\"" + cache.FLUSH + "\"}}");
	}

	public void evict(final String key) {
		mcClient.delete(key.toString());
		LOG.info("{\"cache\":{\"status\":\"" + cache.DELETE + "\",\"key\":\"" + key + "\"}}");
	}

	public byte[] get(final String key) {
		byte[] value = null;
		try {
			value = (byte[]) mcClient.get(key);
		} catch (final Exception e) {
			LOG.error(e.getMessage());
		}
		if (value == null) {
			LOG.info("{\"cache\":{\"status\":\"" + cache.MISS + "\",\"key\":\"" + key + "\"}}");
			return null;
		}
		LOG.info("{\"cache\":{\"status\":\"" + cache.HIT + "\",\"key\":\"" + key + "\"}}");
		return value;
	}

	public void put(final String key, final byte[] value) {
		putWithTtl(key, 3600, value);
	}

	public void putWithTtl(final String key, final int ttl, final byte[] value) {
		if (value != null) {
			mcClient.set(key.toString(), ttl, value);
			LOG.info("{\"cache\":{\"status\":\"" + cache.PUT + "\",\"key\":\"" + key + "\"}}");
		}
	}

	public void close() {
		mcClient.shutdown();
	}

}
