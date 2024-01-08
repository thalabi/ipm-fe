package com.kerneldc.ipm.domain.converter;

import com.kerneldc.ipm.domain.FinancialInstitutionEnum;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FinancialInstitutionEnumConverter implements AttributeConverter<FinancialInstitutionEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(FinancialInstitutionEnum financialInstitutionEnum) {
		return financialInstitutionEnum == null ? null : financialInstitutionEnum.getInstitutionNumber() * 100000 + financialInstitutionEnum.getTransitNumber();
	}

	@Override
	public FinancialInstitutionEnum convertToEntityAttribute(Integer institutionNumberColumn) {
		return institutionNumberColumn == null ? null : FinancialInstitutionEnum.financialInstitutionEnumOf(institutionNumberColumn);
	}
}
