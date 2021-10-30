package com.kerneldc.common.domain.converter;

import java.security.Key;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Converter
@Component
@Slf4j
public class HmacSHA256KeyConverter implements AttributeConverter<Key, String>{

	@Override
	public String convertToDatabaseColumn(Key attribute) {
		if (attribute == null) {
			return null;
		}
		String base64EncodedKey = Base64.getEncoder().encodeToString(attribute.getEncoded());
		LOGGER.info("converted key to table column: {}, length:{}", base64EncodedKey, base64EncodedKey.length());
		return base64EncodedKey;
	}

	@Override
	public Key convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return null;
		}
		byte[] decodedKey = Base64.getDecoder().decode(dbData);
		SecretKey secretKeyResult = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
		LOGGER.info("converted table column to key: {}", secretKeyResult);
		return secretKeyResult;
	}

}
