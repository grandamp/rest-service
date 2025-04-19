package org.keysupport.api.pkix.cache.singletons;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses a singleton pattern to store the configured CRL cache.
 */
public class CRLCacheSingleton {

	private final Logger LOG = LoggerFactory.getLogger(CRLCacheSingleton.class);

	/**
	 * A map of intermediate stores that correspond to each validation policy.
	 */
	private ConcurrentHashMap<String, byte[]> crlMap = null;

	ObjectMapper mapper = null;

	private CRLCacheSingleton() {
		/*
		 * Collect stats for revocation data
		 */
		int totalCrls = 0;
		int totalCrlSize = 0;
		int totalCrlRevoked = 0;
		/*
		 * Create global crlMap
		 */
		crlMap = new ConcurrentHashMap<String, byte[]>();
		mapper = new ObjectMapper();
		List<Path> crlPaths = getCachedCRLs();
		for (Path currentPath: crlPaths) {
			int revokedCerts = 0;
			String crlPath = currentPath.toString();
			LOG.info("Loadinng CRL: " + crlPath);
			X509CRL crl = readCRLFromFile(crlPath);
			totalCrls++;
			int crlSize = 0;
			try {
				crlSize = crl.getEncoded().length;
			} catch (CRLException e) {
				LOG.error("Unable to decode CRL", e);
			}
			if (crlSize == 0) {
				LOG.warn("CRL file contains no bytes: " + crlPath);
			} 
			totalCrlSize = totalCrlSize + crlSize;
			String issuer_name = crl.getIssuerX500Principal().getName();
			String kid = null;
			/*
			 * Lack of an authorityKeyIidentifier extension is a deal breaker for consuming CRL data
			 */
			byte[] authorityKeyIdentifier = crl.getExtensionValue("2.5.29.35");
			if (null == authorityKeyIdentifier) {
				LOG.warn("CRL " + crlPath + "does not contain an authorityKeyIdentifier extension!");
			} else {
				LOG.debug("AuthorityKeyIdentifier:" + X509Util.byteArrayToHexString(authorityKeyIdentifier));
				ASN1OctetString authorityKeyId = ASN1OctetString.getInstance(authorityKeyIdentifier);
				AuthorityKeyIdentifier akid = AuthorityKeyIdentifier.getInstance(authorityKeyId.getOctets());
				byte[] authorityKeyIdBytes = akid.getKeyIdentifier();
				kid = X509Util.byteArrayToHexString(authorityKeyIdBytes).toLowerCase();
				LOG.info("CRL Issuer: " + issuer_name);
				LOG.debug("kid:" + kid);
				LOG.debug("thisUpdate: " + crl.getThisUpdate());
				LOG.debug("nextUpdate: " + crl.getNextUpdate());
				LOG.info("crlSize: " + crlSize);
				if (null == crl.getRevokedCertificates()) {
					LOG.info("revokedCertCount: 0");
				} else {
					revokedCerts = crl.getRevokedCertificates().size();
					LOG.info("revokedCertCount: " + revokedCerts);
					totalCrlRevoked = totalCrlRevoked + revokedCerts;
				}
				try {
					crlMap.put(kid, crl.getEncoded());
				} catch (CRLException e) {
					LOG.error("Error rendering CRL to byte[]", e);
				}
				LOG.info("totalCrls: " + totalCrls);
				LOG.info("totalCrlSize: " + totalCrlSize);
				LOG.info("totalCrlRevoked: " + totalCrlRevoked);
			}
		}
	}

	public X509CRL getCrl(String kid) {
		byte[] encodedCrl = crlMap.get(kid);
		return renderX509Crl(encodedCrl);
	}

	private X509CRL renderX509Crl(byte[] crlBytes) {
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error("Error creating Certificate Factory", e);
		}
		InputStream in = new ByteArrayInputStream(crlBytes);
		X509CRL crl = null;
		try {
			crl = (X509CRL) cf.generateCRL(in);
		} catch (CRLException e) {
			LOG.error("Error rendering CRL", e);
		}
		return crl;
	}

	public static List<Path> getCachedCRLs() {
		Path directoryPath = Paths.get("/opt/vss/crls");
		List<Path> crlFiles = null;
		try (Stream<Path> paths = Files.walk(directoryPath)) {
			crlFiles = paths
					.filter(Files::isRegularFile)
					.filter(path -> path
					.toString()
					.toLowerCase()
					.endsWith(".crl"))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return crlFiles;
	}

	public static X509CRL readCRLFromFile(String filePath) {
		File crlFile = new File(filePath);
		X509CRL crl = null;
		InputStream is = null;
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			is = new FileInputStream(crlFile);
			crl = (X509CRL) cf.generateCRL(is);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CRLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ignored) {
				}
			}
		}
		return crl;
	}

	public void updateIntermediates(ValidationPolicy valPol) {
	}

	private class SingletonHelper {
		private static final CRLCacheSingleton INSTANCE = new CRLCacheSingleton();
	}

	public static CRLCacheSingleton getInstance() {
		/*
		 * TODO: Return to singleton
		 * 
		 * For now, we will always return a new Instance/Object
		 */
		return SingletonHelper.INSTANCE;
		//return new CRLCacheSingleton();
	}

}
