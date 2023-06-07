package com.kerneldc.common.domain.converter;

import javax.persistence.AttributeConverter;

// Disable for postgres
//@Converter(autoApply = true)
public class BooleanConverter implements AttributeConverter<Boolean, Character> {

	@Override
	public Character convertToDatabaseColumn(Boolean attribute) {

	    if (attribute != null) {
            if (attribute) {
                return 'y';
            } else {
                return 'n';
            }
        }
        return null;
	}

	@Override
	public Boolean convertToEntityAttribute(Character dbData) {
	    if (dbData != null) {
            return dbData.equals('y');
        }
        return false;	
    }
}
