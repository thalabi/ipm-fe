package com.kerneldc.ipm.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import com.kerneldc.common.domain.AbstractImmutableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "instrument_due_v")
@Immutable
@Getter
public class InstrumentDueV extends AbstractImmutableEntity {
	
	private Integer portfolioFi;
	@Transient
	@Setter
	private String portfolioFiName; // set to FinancialInstitutionEnum name
	
	private String portfolioName;
	private String accountNumber;
	private String instrumentName;
	//private String ticker;
	private String currency;
	private String type;
	
	private Integer issuerFi;
	@Transient
	@Setter
	private String issuerFiName; // set to FinancialInstitutionEnum name
	
	private String term;
	private BigDecimal price;
	private BigDecimal quantity;
	private Float interestRate;
	private String maturityDate;
	private Float promotionalInterestRate;
	private String promotionEndDate;
	
	private String issuerEntity;
	private String cusip;
	private Float coupon;
	private String issueDate;
	private String paymentFrequency;
	private String nextPaymentDate;
	
	private Boolean emailNotification;
	private String dueDate;
	
	@Transient
	@Setter
	private Boolean overdue;
}