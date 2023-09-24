package com.kerneldc.ipm.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
	private String portfolioHolder;
	@Transient
	@Setter
	private String portfolioFiName; // set to FinancialInstitutionEnum name
	
	private String portfolioName;
	private String portfolioAccountNumber;
	private String instrumentName;
	private String instrumentAccountNumber;
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
	private OffsetDateTime maturityDate;
	private Float promotionalInterestRate;
	private OffsetDateTime promotionEndDate;
	
	private String issuerEntity;
	private String cusip;
	private Float coupon;
	private OffsetDateTime issueDate;
	private String paymentFrequency;
	private OffsetDateTime nextPaymentDate;
	
	private Boolean emailNotification;
	private String notes;
	private OffsetDateTime dueDate;
	
	@Transient
	@Setter
	private Boolean overdue;
}