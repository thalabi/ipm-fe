package com.kerneldc.ipm.domain.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.kerneldc.ipm.domain.FinancialInstitutionEnum;

@Converter(autoApply = true)
public class FinancialInstitutionEnumConverter implements AttributeConverter<FinancialInstitutionEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(FinancialInstitutionEnum financialInstitutionEnum) {
		return financialInstitutionEnum == null ? null : financialInstitutionEnum.getInstitutionNumber();
	}

	@Override
	public FinancialInstitutionEnum convertToEntityAttribute(Integer institutionNumber) {
		return institutionNumber == null ? null : FinancialInstitutionEnum.financialInstitutionEnumOf(institutionNumber);
	}
}
