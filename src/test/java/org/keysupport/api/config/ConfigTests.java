package org.keysupport.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;

import io.swagger.v3.oas.models.OpenAPI;

class ConfigTests {

	@AfterEach
	void clearSystemProperties() {
		System.clearProperty("com.sun.security.crl.maxSize");
		System.clearProperty("com.sun.security.allowedAIALocations");
		System.clearProperty("com.sun.security.enableCRLDP");
		System.clearProperty("com.sun.security.enableAIAcaIssuers");
	}

	@Test
	void configurationSwagger_buildsOpenApiMetadata() {
		ConfigurationSwagger swagger = new ConfigurationSwagger();
		OpenAPI api = swagger.springShopOpenAPI();
		assertNotNull(api);
		assertNotNull(api.getInfo());
		assertEquals("Certificate Validation Service API", api.getInfo().getTitle());
		assertTrue(api.getInfo().getVersion() != null && !api.getInfo().getVersion().isBlank());
		assertEquals("https://github.com/grandamp/rest-service/", ConfigurationSwagger.resolveVersion(null));
		assertEquals("https://github.com/grandamp/rest-service/", ConfigurationSwagger.resolveVersion("unknown"));
		assertEquals("ghcr.io/grandamp/rest-service:sha-f9de467",
				ConfigurationSwagger.resolveVersion("f9de467187fac1d8f79bb66df84f0697880f7403"));
	}

	@Test
	void serviceValidationPolicies_handlesValidAndNullInputs() {
		ValidationPolicy p1 = new ValidationPolicy();
		p1.validationPolicyId = "1.3.6.1.5.5.7.19.1";
		ValidationPolicy p2 = new ValidationPolicy();
		p2.validationPolicyId = "2.16.840.1.101.10.2.18.2.1.4";
		ValidationPolicies all = new ValidationPolicies();
		all.validationPolicies = List.of(p1, p2);

		ValidationPoliciesSingleton singleton = mock(ValidationPoliciesSingleton.class);
		when(singleton.getValidationPolicies()).thenReturn(all, all, all, all, null, new ValidationPolicies());

		try (MockedStatic<ValidationPoliciesSingleton> mocked = mockStatic(ValidationPoliciesSingleton.class)) {
			mocked.when(ValidationPoliciesSingleton::getInstance).thenReturn(singleton);

			assertSame(all, ServiceValidationPolicies.getValidationPolicies());
			assertSame(p1, ServiceValidationPolicies.getPolicy("1.3.6.1.5.5.7.19.1"));
			assertSame(p2, ServiceValidationPolicies.getPolicy("2.16.840.1.101.10.2.18.2.1.4"));
			assertNull(ServiceValidationPolicies.getPolicy("9.9.9.9"));
			assertNull(ServiceValidationPolicies.getPolicy(null));
			assertNull(ServiceValidationPolicies.getPolicy("1.3.6.1.5.5.7.19.1"));
			assertNull(ServiceValidationPolicies.getPolicy("1.3.6.1.5.5.7.19.1"));
		}
	}

	@Test
	void serviceConfiguration_configuresSingletonsAndSystemProperties(@TempDir Path tempDir) throws Exception {
		ValidationPoliciesSingleton polSingleton = mock(ValidationPoliciesSingleton.class);
		IntermediateCacheSingleton cacheSingleton = mock(IntermediateCacheSingleton.class);

		PrintStream origOut = System.out;
		PrintStream origErr = System.err;
		Path sysLog = tempDir.resolve("system.log");

		try (MockedStatic<ValidationPoliciesSingleton> polMock = mockStatic(ValidationPoliciesSingleton.class);
				MockedStatic<IntermediateCacheSingleton> cacheMock = mockStatic(IntermediateCacheSingleton.class)) {
			polMock.when(ValidationPoliciesSingleton::getInstance).thenReturn(polSingleton);
			cacheMock.when(IntermediateCacheSingleton::getInstance).thenReturn(cacheSingleton);

			new ServiceConfiguration(
					"https://example.test/policies.json",
					"https://example.test/intermediates.p7b",
					7,
					true,
					sysLog.toString(),
					true,
					"http://crl.example.test",
					true,
					true,
					true,
					true,
					52428800,
					"SUN",
					"PKIX");

			verify(polSingleton).updateValidationPolicies("https://example.test/policies.json");
			verify(polSingleton).setMaxPathLen(7);
			verify(polSingleton).setAiaChase(true);
			verify(polSingleton).setAllowedAiaLocations("http://crl.example.test");
			verify(polSingleton).setRevocationEnabled(true);
			verify(polSingleton).setRevocationEeOnly(true);
			verify(polSingleton).setOcspEnabled(true);
			verify(polSingleton).setCrlEnabled(true);
			verify(polSingleton).setCrlMaxSize(52428800);
			verify(polSingleton).setCertPathProvider("SUN");
			verify(polSingleton).setCertPathAlgorithm("PKIX");
			verify(polSingleton).setIntermediateValidateOnLoad(true);
			verify(polSingleton).setIntermediateRevocationEnabled(true);
			verify(polSingleton).lockConfiguration();
			verify(cacheSingleton).updateIntermediates("https://example.test/intermediates.p7b");
			verify(cacheSingleton).startPeriodicRevalidation(3600L);

			assertEquals("52428800", System.getProperty("com.sun.security.crl.maxSize"));
			assertEquals("http://crl.example.test", System.getProperty("com.sun.security.allowedAIALocations"));
			assertTrue(Files.exists(sysLog));
		} finally {
			System.setOut(origOut);
			System.setErr(origErr);
		}
	}

	@Test
	void serviceConfiguration_failsFastOnBlankUrisAndKeepsStdStreamsIfLogFileUnwritable(@TempDir Path tempDir) {
		ValidationPoliciesSingleton polSingleton = mock(ValidationPoliciesSingleton.class);
		IntermediateCacheSingleton cacheSingleton = mock(IntermediateCacheSingleton.class);

		PrintStream origOut = System.out;
		PrintStream origErr = System.err;
		try (MockedStatic<ValidationPoliciesSingleton> polMock = mockStatic(ValidationPoliciesSingleton.class);
				MockedStatic<IntermediateCacheSingleton> cacheMock = mockStatic(IntermediateCacheSingleton.class)) {
			polMock.when(ValidationPoliciesSingleton::getInstance).thenReturn(polSingleton);
			cacheMock.when(IntermediateCacheSingleton::getInstance).thenReturn(cacheSingleton);

			assertThrows(IllegalStateException.class, () -> new ServiceConfiguration(
					" ", "https://example.test/intermediates.p7b", 7, false, "",
					false, "any", true, true, true, true, 50, "SUN", "PKIX"));

			assertThrows(IllegalStateException.class, () -> new ServiceConfiguration(
					"https://example.test/policies.json", "", 7, false, "",
					false, "any", true, true, true, true, 52428800, "SUN", "PKIX"));

			Path missingDirFile = tempDir.resolve("nonexistent-dir").resolve("system.log");
			new ServiceConfiguration(
					"https://example.test/policies.json",
					"https://example.test/intermediates.p7b",
					7,
					true,
					missingDirFile.toString(),
					false,
					"any",
					true,
					true,
					true,
					true,
					50,
					"SUN",
					"PKIX");

			assertEquals("50", System.getProperty("com.sun.security.crl.maxSize"));

			assertNotNull(System.out);
			assertNotNull(System.err);
			assertSame(origOut, System.out);
			assertSame(origErr, System.err);
		} finally {
			System.setOut(origOut);
			System.setErr(origErr);
		}
	}

	@Test
	void serviceConfiguration_hasSingleAutowiredConstructor() {
		AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
		var ctors = processor.determineCandidateConstructors(ServiceConfiguration.class, "serviceConfiguration");
		assertNotNull(ctors);
		assertEquals(1, ctors.length);
		assertEquals(22, ctors[0].getParameterCount());
	}
}
