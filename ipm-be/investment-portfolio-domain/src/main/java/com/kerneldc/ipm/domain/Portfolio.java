package com.kerneldc.ipm.domain;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
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
	@Enumerated(EnumType.STRING)
	@CsvBindByName
	private HolderEnum holder;
	@CsvBindByName(column = "portfolio_id")
	@Setter(AccessLevel.NONE)
	private String portfolioId;
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
	public void setPortfolioId(String portfolioId) {
		this.portfolioId = portfolioId;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		LOGGER.info("setLogicalKeyHolder(), financialInstitution: {}", financialInstitution);
		var financialInstitutionNumber = financialInstitution == null ? 0 : financialInstitution.getInstitutionNumber();
		var logicalKeyHolder = LogicalKeyHolder.build(financialInstitutionNumber, portfolioId);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}