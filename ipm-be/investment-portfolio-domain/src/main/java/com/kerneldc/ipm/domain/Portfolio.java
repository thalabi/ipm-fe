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

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "portfolio_seq", allocationSize = 1)
@Getter @Setter
public class Portfolio extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String institution;
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

	public void setInstitution(String institution) {
		this.institution = institution;
		setLogicalKeyHolder();
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(institution, accountNumber);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
