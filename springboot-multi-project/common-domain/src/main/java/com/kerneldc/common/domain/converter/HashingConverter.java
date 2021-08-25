package com.kerneldc.common.domain.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Converter
@Component
public class HashingConverter implements AttributeConverter<String, String> {
	
	private static final int LENGTH_OF_HASH = 60;
	// @Autowired causes a circular dependency 
	private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public String convertToDatabaseColumn(String attribute) {
        return isAlreadyAHash(attribute) ? attribute : passwordEncoder.encode(attribute);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
        return dbData;	
    }
	
	private boolean isAlreadyAHash(String attribute) {
		return StringUtils.length(attribute) == LENGTH_OF_HASH;
	}
}
