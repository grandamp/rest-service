package org.keysupport.api.pkix;

import java.util.UUID;

public class DerivePolicyIDFromName {

	public static void main(String[] args) {
		String name = "aal3";
		UUID nameUuid = X509Util.uuidFromName(name);
		System.out.println("      \"validationPolicyId\": \"" + nameUuid + "\",\n"
				+ "      \"validationPolicyName\": \"" + name + "\",\n");
	}

}
