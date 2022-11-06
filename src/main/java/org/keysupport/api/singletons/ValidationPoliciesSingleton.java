package org.keysupport.api.singletons;

import java.nio.charset.StandardCharsets;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * This class uses a singleton pattern to store the configured Trust Anchors.
 */
public class ValidationPoliciesSingleton {

	private final static Logger LOG = LoggerFactory.getLogger(ValidationPoliciesSingleton.class);

	private static String s3Bucket = null;

	private final static String validationPoliciesKey = "configuration/policies.json";

	private static ValidationPolicies validationPolicies = null;

	private ValidationPoliciesSingleton() {
        /*
         * If running locally, set the S3_BUCKET variable locally first.
         *
         * export S3_BUCKET=<s3 bucket name>
         *
         * In our EBS environment, we expect this variable to be set:
         *
		 *   - https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-platform.html
         */
        s3Bucket = System.getenv("S3_BUCKET");
        if (null == s3Bucket) {
        	LOG.error("S3_BUCKET Not Set!");
        }
        /*
         * Create our S3 Client
         */
        S3Client s3 = S3Client.create();
        /*
         * Set our ValidationPolicies
         */
        try {
            GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(validationPoliciesKey)
                .bucket(s3Bucket)
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();
            String validationPoliciesJson = new String(data, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            validationPolicies = mapper.readValue(validationPoliciesJson, ValidationPolicies.class);
            LOG.info("ValidationPolicies: " + validationPoliciesJson);
        } catch (S3Exception e) {
        	LOG.error("Error fetching TrustAnchorsSingleton from S3", e);
        } catch (JsonMappingException e) {
        	LOG.error("Error creating ValidationPolicies Object", e);
		} catch (JsonProcessingException e) {
        	LOG.error("Error creating ValidationPolicies Object", e);
		}
        /*
         * Close our S3 resource
         */
		s3.close();
	}

	private static class SingletonHelper {
		private static final ValidationPoliciesSingleton INSTANCE = new ValidationPoliciesSingleton();
	}

	public static ValidationPoliciesSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public ValidationPolicies getValidationPolicies() {
		return validationPolicies;
	}

}
