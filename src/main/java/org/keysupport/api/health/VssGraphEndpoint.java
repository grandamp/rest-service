package org.keysupport.api.health;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

/**
 * Read-only Actuator endpoint exposing per-policy FPKI trust graph telemetry
 * derived from cached intermediates and configured validation policies.
 */
@Component
@Endpoint(id = "vssgraph")
public class VssGraphEndpoint {

	private static final String JCE_PROVIDER = BouncyCastleFipsProvider.PROVIDER_NAME;
	private static final Set<String> GENERIC_OUS = Set.of(
			"certification authorities", "pki", "fpki", "certservers", "u.s. government");
	private static final String[] KEY_USAGE_NAMES = {
			"digitalSignature", "nonRepudiation", "keyEncipherment", "dataEncipherment",
			"keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly"
	};

	private final Object cacheLock = new Object();
	private volatile ValidationPolicies lastPoliciesRef;
	private volatile CertStore lastStoreRef;
	private volatile int lastCertCount = -1;
	private volatile Map<String, Object> cachedSnapshot;

	@ReadOperation
	public Map<String, Object> graph() {
		ValidationPoliciesSingleton policiesSingleton = ValidationPoliciesSingleton.getInstance();
		IntermediateCacheSingleton intermediateSingleton = IntermediateCacheSingleton.getInstance();

		ValidationPolicies policies = policiesSingleton.getValidationPolicies();
		CertStore store = intermediateSingleton.getIntermediates();
		List<X509Certificate> cachedCerts = intermediateSingleton.getCachedCerts();

		Map<String, Object> snapshot = this.cachedSnapshot;
		if (null != snapshot && policies == this.lastPoliciesRef && store == this.lastStoreRef
				&& cachedCerts.size() == this.lastCertCount) {
			return snapshot;
		}

		synchronized (cacheLock) {
			if (null != this.cachedSnapshot && policies == this.lastPoliciesRef && store == this.lastStoreRef
					&& cachedCerts.size() == this.lastCertCount) {
				return this.cachedSnapshot;
			}
			Map<String, Object> built = buildSnapshot(policiesSingleton, intermediateSingleton, policies, store, cachedCerts);
			this.lastPoliciesRef = policies;
			this.lastStoreRef = store;
			this.lastCertCount = cachedCerts.size();
			this.cachedSnapshot = built;
			return built;
		}
	}

	private Map<String, Object> buildSnapshot(
			ValidationPoliciesSingleton policiesSingleton,
			IntermediateCacheSingleton intermediateSingleton,
			ValidationPolicies policies,
			CertStore store,
			List<X509Certificate> cachedCerts) {
		ensureBcFipsProvider();
		Date now = new Date();

		Map<String, X509Certificate> certById = new LinkedHashMap<>();
		List<ValidationPolicy> policyList = (null != policies && null != policies.validationPolicies)
				? policies.validationPolicies
				: List.of();

		for (ValidationPolicy policy : policyList) {
			Set<TrustAnchor> anchors = policiesSingleton.getTrustAnchors(policy.validationPolicyId);
			if (null == anchors) {
				continue;
			}
			for (TrustAnchor ta : anchors) {
				X509Certificate taCert = ta.getTrustedCert();
				if (null != taCert) {
					certById.putIfAbsent(X509Util.x5tS256(taCert), taCert);
				}
			}
		}
		for (X509Certificate cert : cachedCerts) {
			certById.putIfAbsent(X509Util.x5tS256(cert), cert);
		}

		Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
		Map<X500Principal, List<Map.Entry<String, X509Certificate>>> bySubject = new LinkedHashMap<>();
		for (Map.Entry<String, X509Certificate> entry : certById.entrySet()) {
			nodes.put(entry.getKey(), toNodeMetadata(entry.getKey(), entry.getValue()));
			bySubject.computeIfAbsent(entry.getValue().getSubjectX500Principal(), k -> new ArrayList<>()).add(entry);
		}

		List<Map<String, String>> allEdges = new ArrayList<>();
		Set<String> seenEdges = new HashSet<>();
		for (Map.Entry<String, X509Certificate> childEntry : certById.entrySet()) {
			X509Certificate child = childEntry.getValue();
			String childId = childEntry.getKey();
			List<Map.Entry<String, X509Certificate>> potentialIssuers = bySubject.get(child.getIssuerX500Principal());
			if (null == potentialIssuers) {
				continue;
			}
			for (Map.Entry<String, X509Certificate> issuerEntry : potentialIssuers) {
				String issuerId = issuerEntry.getKey();
				if (issuerId.equals(childId)) {
					continue;
				}
				X509Certificate issuer = issuerEntry.getValue();
				try {
					child.verify(issuer.getPublicKey(), JCE_PROVIDER);
					String edgeKey = issuerId + "->" + childId;
					if (seenEdges.add(edgeKey)) {
						allEdges.add(Map.of("source", issuerId, "target", childId));
					}
				} catch (GeneralSecurityException ignored) {
					// Same Subject DN with a non-matching key (e.g. rekeyed CA); skip non-verifying edge.
				}
			}
		}

		List<Map<String, Object>> policyGraphs = new ArrayList<>(policyList.size());
		for (ValidationPolicy policy : policyList) {
			policyGraphs.add(buildPolicyGraph(policy, policiesSingleton, store, cachedCerts, nodes, allEdges, now));
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("generatedAt", X509Util.ISO8601DateString(now));
		result.put("nodeCount", nodes.size());
		result.put("edgeCount", allEdges.size());
		result.put("nodes", nodes);
		result.put("edges", allEdges);
		result.put("policies", policyGraphs);
		result.put("excludedFromBundle", intermediateSingleton.getLastExcludedCerts());
		return Collections.unmodifiableMap(result);
	}

	private Map<String, Object> buildPolicyGraph(
			ValidationPolicy policy,
			ValidationPoliciesSingleton policiesSingleton,
			CertStore store,
			List<X509Certificate> cachedCerts,
			Map<String, Map<String, Object>> nodes,
			List<Map<String, String>> allEdges,
			Date now) {
		Set<TrustAnchor> taList = policiesSingleton.getTrustAnchors(policy.validationPolicyId);
		Set<String> anchorIds = new LinkedHashSet<>();
		if (null != taList) {
			for (TrustAnchor ta : taList) {
				if (null != ta.getTrustedCert()) {
					anchorIds.add(X509Util.x5tS256(ta.getTrustedCert()));
				}
			}
		}

		Set<String> reachableIds = new LinkedHashSet<>(anchorIds);
		List<Map<String, String>> excludedForPolicy = new ArrayList<>();
		Map<String, List<String>> validPolicyOidsByNode = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> policyLeavesByNode = new LinkedHashMap<>();

		for (String anchorId : anchorIds) {
			List<String> anchorPolicies = null != policy.userPolicySet ? List.copyOf(policy.userPolicySet) : List.of();
			validPolicyOidsByNode.put(anchorId, anchorPolicies);
		}

		for (X509Certificate cert : cachedCerts) {
			String certId = X509Util.x5tS256(cert);
			if (anchorIds.contains(certId)) {
				continue;
			}
			PathValidationOutcome outcome = validateStructuralPath(cert, taList, policy, store, policiesSingleton, now);
			if (null == outcome.failureReason()) {
				reachableIds.add(certId);
				validPolicyOidsByNode.put(certId, outcome.validPolicyOids());
				policyLeavesByNode.put(certId, outcome.policyLeaves());
			} else {
				Map<String, Object> meta = nodes.get(certId);
				String label = null != meta ? String.valueOf(meta.get("label")) : certId;
				excludedForPolicy.add(Map.of(
						"id", certId,
						"label", label,
						"reason", outcome.failureReason()));
			}
		}

		List<Map<String, String>> policyEdges = new ArrayList<>();
		for (Map<String, String> edge : allEdges) {
			if (reachableIds.contains(edge.get("source")) && reachableIds.contains(edge.get("target"))) {
				policyEdges.add(edge);
			}
		}

		Map<String, Object> pg = new LinkedHashMap<>();
		pg.put("validationPolicyId", policy.validationPolicyId);
		pg.put("validationPolicyName", policy.validationPolicyName);
		pg.put("validationPolicyDescription", policy.validationPolicyDescription);
		pg.put("userPolicySet", null != policy.userPolicySet ? policy.userPolicySet : List.of());
		pg.put("inhibitPolicyMapping", policy.inhibitPolicyMapping);
		pg.put("requireExplicitPolicy", policy.requireExplicitPolicy);
		pg.put("inhibitAnyPolicy", policy.inhibitAnyPolicy);
		pg.put("anchors", new ArrayList<>(anchorIds));
		pg.put("reachableNodes", new ArrayList<>(reachableIds));
		pg.put("edges", policyEdges);
		pg.put("excludedForPolicy", excludedForPolicy);
		pg.put("validPolicyOids", validPolicyOidsByNode);
		pg.put("policyLeaves", policyLeavesByNode);
		return pg;
	}

	private record PathValidationOutcome(
			String failureReason,
			List<String> validPolicyOids,
			List<Map<String, Object>> policyLeaves) {
	}

	private PathValidationOutcome validateStructuralPath(
			X509Certificate cert,
			Set<TrustAnchor> taList,
			ValidationPolicy policy,
			CertStore intermediateStore,
			ValidationPoliciesSingleton policies,
			Date now) {
		if (null == taList || taList.isEmpty()) {
			return new PathValidationOutcome("Policy has no trust anchors", List.of(), List.of());
		}
		String provider = null != policies.getCertPathProvider() && !policies.getCertPathProvider().isBlank()
				? policies.getCertPathProvider()
				: "SUN";
		String algorithm = null != policies.getCertPathAlgorithm() && !policies.getCertPathAlgorithm().isBlank()
				? policies.getCertPathAlgorithm()
				: "PKIX";

		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);

		PKIXBuilderParameters params;
		try {
			params = new PKIXBuilderParameters(taList, selector);
		} catch (InvalidAlgorithmParameterException e) {
			return new PathValidationOutcome("Invalid PKIX parameters: " + e.getLocalizedMessage(), List.of(), List.of());
		}

		params.setSigProvider(JCE_PROVIDER);
		params.setDate(now);
		if (null != policy.userPolicySet) {
			params.setInitialPolicies(new HashSet<>(policy.userPolicySet));
		}
		params.setPolicyMappingInhibited(policy.inhibitPolicyMapping);
		params.setExplicitPolicyRequired(policy.requireExplicitPolicy);
		params.setAnyPolicyInhibited(policy.inhibitAnyPolicy);
		params.setMaxPathLength(policies.getMaxPathLen());
		params.setRevocationEnabled(false);
		if (null != intermediateStore) {
			params.addCertStore(intermediateStore);
		}

		try {
			CertPathBuilder cpb = CertPathBuilder.getInstance(algorithm, provider);
			CertPathValidator cpv = CertPathValidator.getInstance(algorithm, provider);
			PKIXCertPathBuilderResult res = (PKIXCertPathBuilderResult) cpb.build(params);
			PKIXCertPathValidatorResult pvr = (PKIXCertPathValidatorResult) cpv.validate(res.getCertPath(), params);
			int targetDepth = res.getCertPath().getCertificates().size();
			List<String> validOids = new ArrayList<>();
			List<Map<String, Object>> leaves = new ArrayList<>();
			collectPolicyLeaves(pvr.getPolicyTree(), targetDepth, validOids, leaves);
			return new PathValidationOutcome(null, List.copyOf(validOids), List.copyOf(leaves));
		} catch (CertPathBuilderException e) {
			Throwable cause = e.getCause();
			String msg = (null != cause && null != cause.getMessage()) ? cause.getMessage() : e.getLocalizedMessage();
			return new PathValidationOutcome(msg, List.of(), List.of());
		} catch (CertPathValidatorException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
				| NoSuchProviderException e) {
			return new PathValidationOutcome(e.getLocalizedMessage(), List.of(), List.of());
		}
	}

	private static void collectPolicyLeaves(
			PolicyNode node,
			int targetDepth,
			List<String> validOids,
			List<Map<String, Object>> leaves) {
		if (null == node) {
			return;
		}
		var children = node.getChildren();
		if (!children.hasNext()) {
			if (targetDepth == 0 || node.getDepth() == targetDepth) {
				String oid = node.getValidPolicy();
				if (null != oid && !validOids.contains(oid)) {
					validOids.add(oid);
				}
				List<String> expected = null != node.getExpectedPolicies()
						? new ArrayList<>(node.getExpectedPolicies())
						: List.of();
				Collections.sort(expected);
				Map<String, Object> leaf = new LinkedHashMap<>();
				leaf.put("validPolicy", oid);
				leaf.put("expectedPolicies", expected);
				leaf.put("critical", node.isCritical());
				leaves.add(Collections.unmodifiableMap(leaf));
			}
			return;
		}
		while (children.hasNext()) {
			collectPolicyLeaves(children.next(), targetDepth, validOids, leaves);
		}
	}

	private Map<String, Object> toNodeMetadata(String id, X509Certificate cert) {
		X500Name x500 = new X500Name(cert.getSubjectX500Principal().getName(X500Principal.RFC2253));
		String cn = firstRdnValue(x500, BCStyle.CN);
		List<String> ous = allRdnValues(x500, BCStyle.OU);
		List<String> orgs = allRdnValues(x500, BCStyle.O);

		String label = cn;
		if (null == label || label.isBlank()) {
			for (String ou : ous) {
				if (!GENERIC_OUS.contains(ou.toLowerCase())) {
					label = ou;
					break;
				}
			}
		}
		if ((null == label || label.isBlank()) && !ous.isEmpty()) {
			label = ous.get(0);
		}
		if ((null == label || label.isBlank()) && !orgs.isEmpty()) {
			label = orgs.get(0);
		}
		if (null == label || label.isBlank()) {
			label = cert.getSubjectX500Principal().getName();
		}

		List<String> affiliation = new ArrayList<>();
		for (String ou : ous) {
			if (!GENERIC_OUS.contains(ou.toLowerCase()) && !label.equalsIgnoreCase(ou) && !affiliation.contains(ou)) {
				affiliation.add(ou);
			}
		}
		for (String org : orgs) {
			if (!GENERIC_OUS.contains(org.toLowerCase()) && !label.equalsIgnoreCase(org) && !affiliation.contains(org)) {
				affiliation.add(org);
			}
		}

		List<String> keyUsage = new ArrayList<>();
		boolean[] ku = cert.getKeyUsage();
		if (null != ku) {
			for (int i = 0; i < Math.min(ku.length, KEY_USAGE_NAMES.length); i++) {
				if (ku[i]) {
					keyUsage.add(KEY_USAGE_NAMES[i]);
				}
			}
		}

		List<String> aiaCaIssuers = new ArrayList<>();
		List<String> ocspUrls = new ArrayList<>();
		parseAccessDescriptions(cert, Extension.authorityInfoAccess.getId(), aiaCaIssuers, ocspUrls);

		List<String> siaCaRepository = new ArrayList<>();
		parseAccessDescriptions(cert, Extension.subjectInfoAccess.getId(), siaCaRepository, null);

		Map<String, Object> node = new LinkedHashMap<>();
		node.put("id", id);
		node.put("label", label);
		node.put("affiliation", String.join(" · ", affiliation));
		node.put("subject", cert.getSubjectX500Principal().getName());
		node.put("issuer", cert.getIssuerX500Principal().getName());
		node.put("serialNumber", cert.getSerialNumber().toString(16).toUpperCase());
		node.put("notBefore", X509Util.ISO8601DateString(cert.getNotBefore()));
		node.put("notAfter", X509Util.ISO8601DateString(cert.getNotAfter()));
		node.put("basicConstraints", cert.getBasicConstraints());
		node.put("keyUsage", keyUsage);
		node.put("sigAlgName", cert.getSigAlgName());
		node.put("publicKey", describePublicKey(cert.getPublicKey()));
		node.put("certificatePolicies", parseCertificatePolicies(cert));
		node.put("aiaCaIssuers", aiaCaIssuers);
		node.put("ocspUrls", ocspUrls);
		node.put("siaCaRepository", siaCaRepository);
		node.put("crlDpUrls", parseCrlDistributionPoints(cert));
		return node;
	}

	private static String firstRdnValue(X500Name name, org.bouncycastle.asn1.ASN1ObjectIdentifier oid) {
		RDN[] rdns = name.getRDNs(oid);
		if (null == rdns || rdns.length == 0 || null == rdns[0].getFirst()) {
			return null;
		}
		return IETFUtils.valueToString(rdns[0].getFirst().getValue());
	}

	private static List<String> allRdnValues(X500Name name, org.bouncycastle.asn1.ASN1ObjectIdentifier oid) {
		RDN[] rdns = name.getRDNs(oid);
		if (null == rdns || rdns.length == 0) {
			return List.of();
		}
		List<String> values = new ArrayList<>(rdns.length);
		for (RDN rdn : rdns) {
			if (null != rdn.getFirst()) {
				values.add(IETFUtils.valueToString(rdn.getFirst().getValue()));
			}
		}
		return values;
	}

	private static String describePublicKey(PublicKey key) {
		if (key instanceof RSAPublicKey rsa) {
			return "RSA " + rsa.getModulus().bitLength();
		}
		if (key instanceof ECPublicKey ec) {
			return "EC " + ec.getParams().getCurve().getField().getFieldSize();
		}
		return null != key ? key.getAlgorithm() : "Unknown";
	}

	private static List<String> parseCertificatePolicies(X509Certificate cert) {
		byte[] ext = cert.getExtensionValue(Extension.certificatePolicies.getId());
		if (null == ext) {
			return List.of();
		}
		try {
			ASN1OctetString oct = ASN1OctetString.getInstance(ASN1Primitive.fromByteArray(ext));
			CertificatePolicies cps = CertificatePolicies.getInstance(ASN1Primitive.fromByteArray(oct.getOctets()));
			PolicyInformation[] infos = cps.getPolicyInformation();
			List<String> oids = new ArrayList<>(infos.length);
			for (PolicyInformation info : infos) {
				oids.add(info.getPolicyIdentifier().getId());
			}
			return oids;
		} catch (IOException | RuntimeException e) {
			return List.of();
		}
	}

	private static void parseAccessDescriptions(
			X509Certificate cert,
			String extOid,
			List<String> caUrls,
			List<String> ocspUrls) {
		byte[] ext = cert.getExtensionValue(extOid);
		if (null == ext) {
			return;
		}
		try {
			ASN1OctetString oct = ASN1OctetString.getInstance(ASN1Primitive.fromByteArray(ext));
			AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(ASN1Primitive.fromByteArray(oct.getOctets()));
			for (AccessDescription ad : aia.getAccessDescriptions()) {
				GeneralName gn = ad.getAccessLocation();
				if (gn.getTagNo() != GeneralName.uniformResourceIdentifier) {
					continue;
				}
				String uri = gn.getName().toString();
				String method = ad.getAccessMethod().getId();
				if ("1.3.6.1.5.5.7.48.1".equals(method)) {
					if (null != ocspUrls && !ocspUrls.contains(uri)) {
						ocspUrls.add(uri);
					}
				} else if (!caUrls.contains(uri)) {
					caUrls.add(uri);
				}
			}
		} catch (IOException | RuntimeException ignored) {
		}
	}

	private static List<String> parseCrlDistributionPoints(X509Certificate cert) {
		byte[] ext = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
		if (null == ext) {
			return List.of();
		}
		try {
			ASN1OctetString oct = ASN1OctetString.getInstance(ASN1Primitive.fromByteArray(ext));
			CRLDistPoint crl = CRLDistPoint.getInstance(ASN1Primitive.fromByteArray(oct.getOctets()));
			List<String> urls = new ArrayList<>();
			for (DistributionPoint dp : crl.getDistributionPoints()) {
				DistributionPointName dpn = dp.getDistributionPoint();
				if (null == dpn || dpn.getType() != DistributionPointName.FULL_NAME) {
					continue;
				}
				GeneralNames gns = GeneralNames.getInstance(dpn.getName());
				for (GeneralName gn : gns.getNames()) {
					if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
						String uri = gn.getName().toString();
						if (!urls.contains(uri)) {
							urls.add(uri);
						}
					}
				}
			}
			return urls;
		} catch (IOException | RuntimeException e) {
			return List.of();
		}
	}

	private static void ensureBcFipsProvider() {
		if (null == Security.getProvider(JCE_PROVIDER)) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
	}
}
