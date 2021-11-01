package com.kerneldc.ipm.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.RepeatedTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class KeyTest {
	
	@RepeatedTest(10)
	void testToStringAndBack() throws NoSuchAlgorithmException {
		Key secretKey = KeyGenerator.getInstance("HmacSHA256").generateKey();
		String base64EncodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		LOGGER.info("base64EncodedKey: {}, length:{}", base64EncodedKey, base64EncodedKey.length());
		
		byte[] decodedKey = Base64.getDecoder().decode(base64EncodedKey);
		SecretKey secretKeyResult = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
		
		assertThat(secretKeyResult.getEncoded()).isEqualTo(secretKey.getEncoded());
	}
	

}
