package com.kerneldc.ipm.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "portfolio_seq", allocationSize = 1)
@Getter @Setter
@Slf4j
public class Portfolio extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private FinancialInstitutionEnum financialInstitution;
	@CsvBindByName(column = "account_number")
	@Setter(AccessLevel.NONE)
	private String accountNumber;
	@CsvBindByName
	private String name;
	@Enumerated(EnumType.STRING)
	@CsvBindByName
	private CurrencyEnum currency;
	@CsvBindByName
	private Boolean logicallyDeleted;

	public void setFinancialInstitution(FinancialInstitutionEnum financialInstitution) {
		LOGGER.info("setFinancialInstitution(), financialInstitution: {}", financialInstitution);
		this.financialInstitution = financialInstitution;
		setLogicalKeyHolder();
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		LOGGER.info("setLogicalKeyHolder(), financialInstitution: {}", financialInstitution);
		var financialInstitutionNumber = financialInstitution == null ? 0 : financialInstitution.getInstitutionNumber();
		var logicalKeyHolder = LogicalKeyHolder.build(financialInstitutionNumber, accountNumber);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
