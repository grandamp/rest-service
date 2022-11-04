package org.keysupport.api.controller.vss;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;

import org.keysupport.api.config.ConfigurationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoliciesController {

	@GetMapping(path = "/vss/v2/policies", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<ValidationPolicies> policies() {
		/*
		 * Temp test to fetch contents of object in bucket
		 */
		/**
		 * The intent of this test is to load the policies for the service via an S3 bucket.
		 * 
		 * The policies should have a version associated to them.
		 * 
		 * This would allow clients and workers to update policies if a new version is assigned.
		 * 
		 * It may not hurt to consider managing our own buckets with an explicit LifecycleConfiguration.
		 * 
		 * - https://docs.aws.amazon.com/AmazonS3/latest/userguide/how-to-set-lifecycle-configuration-intro.html
		 * 
		 * This will introduce AWS S3 as a dependency (Must have AWS CLI to run locally, or S3 calls will raise exceptions)
		 * 
		 * Our validation policies do not need to change often.  Nor the Trust Anchor(s) or intermediate CAs.
		 * 
		 * CRLs should be the primary source of revocation info, to promote privacy on credential use (as opposed to OCSP).
		 * 
		 * Using S3 for distributed persistence should be used for:
		 * 
		 * - Validation Policies
		 * 	- Rarely Change, but updates to the policies in S3 should require all workers to update the current policy
		 * - Trust Anchors
		 * 	- Only updated on trust anchor rekeys, or; new validation policies that require the trust anchor.
		 * 		- Same lifecycle as Validation Policies
		 * - Intermediate CAs
		 * 	- These artifacts have shorter lifetimes than trust anchors
		 * - Issuing CAs
		 *  - Same lifecycle as Intermediate CAs
		 * - CRLs
		 * 	- These artifacts generally update daily basis, but; depend on the publishing CA type/lifecycle.
		 * 		- I.e., FPKI Intermediates issue a CRL every 30 days, Issuers every 24 hours minimum.
		 * 		- S3 should only be used as a cache that can be cleared at any time, and;
		 * 			- is periodically updated with the freshest CRL 
		 * 
		 */
		ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
		Region region = Region.US_EAST_1;
		S3Client s3 = S3Client.builder().region(region).credentialsProvider(credentialsProvider).build();
        try {
            GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key("api_config/policies.json")
                .bucket("elasticbeanstalk-us-east-1-216896468348")
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            
            byte[] data = objectBytes.asByteArray();
            String policyJson = new String(data, StandardCharsets.UTF_8);
            System.out.println(policyJson);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
		s3.close();
		/*
		 * End Temp code
		 */
		return new ResponseEntity<>(ConfigurationPolicies.getValidationPolicies(), HttpStatus.OK);
	}

}
