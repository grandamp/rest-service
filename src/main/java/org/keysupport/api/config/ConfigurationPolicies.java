package org.keysupport.api.config;

import java.util.ArrayList;
import java.util.List;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;

public class ConfigurationPolicies {

	public static ValidationPolicies getValidationPolicies() {

		ValidationPolicies validationPolicies = new ValidationPolicies();
		List<ValidationPolicy> policies = new ArrayList<>();
		
		/*
		 * Default policy Prod and Cite
		 */
		ValidationPolicy defaultPol = new ValidationPolicy();
		defaultPol.validationPolicyId = "1.3.6.1.5.5.7.19.1";
		defaultPol.trustAnchors = ConfigurationTrustAnchors.getTrustAnchors().trustAnchors;
		List<String> userPoliciesDefault = new ArrayList<>();
		userPoliciesDefault.add("2.5.29.32.0");
		defaultPol.userPolicySet = userPoliciesDefault;
		defaultPol.inhibitPolicyMapping = false;
		defaultPol.requireExplicitPolicy = false;
		defaultPol.inhibitAnyPolicy = true;
		policies.add(defaultPol);
		/*
		 * AAL3 Prod and Cite
		 */
		ValidationPolicy aal3ProdCitePol = new ValidationPolicy();
		aal3ProdCitePol.validationPolicyId = "2.16.840.1.101.10.2.18.2.2.1004";
		aal3ProdCitePol.trustAnchors = ConfigurationTrustAnchors.getTrustAnchors().trustAnchors;
		List<String> userPoliciesAal3ProdCite = new ArrayList<>();
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.3.7");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.3.13");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.3.15");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.3.16");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.3.18");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.3.41");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.48.9");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.48.11");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.48.6");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.48.12");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.48.78");
		userPoliciesAal3ProdCite.add("2.16.840.1.101.3.2.1.48.110");
		aal3ProdCitePol.userPolicySet = userPoliciesAal3ProdCite;
		aal3ProdCitePol.inhibitPolicyMapping = false;
		aal3ProdCitePol.requireExplicitPolicy = true;
		aal3ProdCitePol.inhibitAnyPolicy = true;
		policies.add(aal3ProdCitePol);
		/*
		 * Default Prod
		 */
		ValidationPolicy defaultProdPol = new ValidationPolicy();
		defaultProdPol.validationPolicyId = "2.16.840.1.101.10.2.18.2.1.5";
		defaultProdPol.trustAnchors = ConfigurationTrustAnchors.getFcpcaG2().trustAnchors;
		List<String> userPoliciesDefaultProd = new ArrayList<>();
		userPoliciesDefaultProd.add("2.5.29.32.0");
		defaultProdPol.userPolicySet = userPoliciesDefaultProd;
		defaultProdPol.inhibitPolicyMapping = false;
		defaultProdPol.requireExplicitPolicy = false;
		defaultProdPol.inhibitAnyPolicy = true;
		policies.add(defaultProdPol);
		/*
		 * AAL3 Prod
		 */
		ValidationPolicy aal3ProdPol = new ValidationPolicy();
		aal3ProdPol.validationPolicyId = "2.16.840.1.101.10.2.18.2.1.4";
		aal3ProdPol.trustAnchors = ConfigurationTrustAnchors.getFcpcaG2().trustAnchors;
		List<String> userPoliciesAal3Prod = new ArrayList<>();
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.7");
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.7");
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.13");
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.15");
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.16");
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.18");
		userPoliciesAal3Prod.add("2.16.840.1.101.3.2.1.3.41");
		aal3ProdPol.userPolicySet = userPoliciesAal3Prod;
		aal3ProdPol.inhibitPolicyMapping = false;
		aal3ProdPol.requireExplicitPolicy = false;
		aal3ProdPol.inhibitAnyPolicy = true;
		policies.add(defaultProdPol);
		
		validationPolicies.validationPolicies = policies;
		return validationPolicies;

	}
}
