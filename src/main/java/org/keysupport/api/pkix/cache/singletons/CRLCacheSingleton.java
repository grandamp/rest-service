package org.keysupport.api.pkix.cache.singletons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.Collection;
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
	private ConcurrentHashMap<String, X509CRL> crlMap = null;

	ObjectMapper mapper = null;

	private CRLCacheSingleton() {
		/*
		 * Since we are the constructor, we will initialize every time for memory testing.
		 * 
		 * For now, we will update all CRLs on disk/volume, and is non-blocking, so our crlMap *could* be unstable during re-initialization
		 */
		//if (null == crlMap) {
		//	crlMap = new ConcurrentHashMap<String, X509CRL>();
		//}
		crlMap = new ConcurrentHashMap<String, X509CRL>();
		mapper = new ObjectMapper();
		List<Path> crlPaths = getCachedCRLs();
		//Collection<CRL> crls = new ArrayList<CRL>();
		for (Path currentPath: crlPaths) {
			LOG.info("Loadinng CRL: " + currentPath.toString());
			String crlPath = currentPath.toString();
			Collection<CRL> currentCRL = readCRLFromFile(crlPath);
			/*
			 * We are assuming one CRL object per CRL file from disk/volume
			 * 
			 * TODO: Perhaps convert to List<X509CRL>
			 * 
			 */
			X509CRL[] crls = currentCRL.toArray(new X509CRL[currentCRL.size()]);
			X509CRL crl = crls[0];
			String issuer_name = crl.getIssuerX500Principal().getName();
			String kid = null;
			LOG.info("CRL Issuer: " + issuer_name);
			byte[] authorityKeyIdentifier = crl.getExtensionValue("2.5.29.35");
			if (null == authorityKeyIdentifier) {
				LOG.warn("CRL " + crlPath + "does not contain an authorityKeyIdentifier extension!");
			} else {
				LOG.info("AuthorityKeyIdentifier:" + X509Util.byteArrayToHexString(authorityKeyIdentifier));
				ASN1OctetString authorityKeyId = ASN1OctetString.getInstance(authorityKeyIdentifier);
				AuthorityKeyIdentifier akid = AuthorityKeyIdentifier.getInstance(authorityKeyId.getOctets());
				byte[] authorityKeyIdBytes = akid.getKeyIdentifier();
				kid = X509Util.byteArrayToHexString(authorityKeyIdBytes).toLowerCase();
				LOG.info("kid:" + kid);
				LOG.info("thisUpdate: " + crl.getThisUpdate());
				LOG.info("nextUpdate: " + crl.getNextUpdate());
				crlMap.put(kid, crl);
			}
		}
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

	public static Collection<CRL> readCRLFromFile(String filePath) {
		File crlFile = new File(filePath);
		// if (!crlFile.isAbsolute()) {
		// crlFile = new File(System.getProperty("catalina.base"), filePath);
		// }
		Collection<? extends CRL> crls = null;
		InputStream is = null;
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			is = new FileInputStream(crlFile);
			crls = cf.generateCRLs(is);
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
		return (Collection<CRL>) crls;
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
		//return SingletonHelper.INSTANCE;
		return new CRLCacheSingleton();
	}

//	public CertStore getIntermediates(String validationPolicyId) {
//		return crlMap.get(validationPolicyId);
//	}

}
