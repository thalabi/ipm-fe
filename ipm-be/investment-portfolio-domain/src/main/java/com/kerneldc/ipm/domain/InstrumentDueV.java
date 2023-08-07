package com.kerneldc.ipm.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import com.kerneldc.common.domain.AbstractImmutableEntity;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "instrument_due_v")
@Immutable
@Getter
public class InstrumentDueV extends AbstractImmutableEntity {
	
//	@CsvBindByName(column = "ticker_exchange")
//	@Description("columnDisplayOrder=1,title=Ticker,sortOrder=1") // sortDirection=desc is also available to specify direction
//	private String tickerExchange;
//	@CsvBindByName(column = "instrument_name")
//	@Description("columnDisplayOrder=2")
//	private String instrumentName;
//	@CsvBindByName
//	@Description("columnDisplayOrder=3,title=Qty")
//	private Float quantity;
//	@CsvBindByName(column = "account_number")
//	@Description("columnDisplayOrder=4,title=Acc No,sortOrder=2")
//	private String accountNumber;
//	@CsvBindByName(column = "account_name")
//	@Description("columnDisplayOrder=5,title=Acc Name")
//	private String accountName;
	
	@CsvBindByName
	private String institution;
	private String portfolioName;
	private String accountNumber;
	private String instrumentName;
	private String ticker;
	private String currency;
	private String type;
	private String financialInstitution;
	private String term;
	private BigDecimal price;
	private BigDecimal quantity;
	private Float interestRate;
	private String maturityDate;
	private Float promotionalInterestRate;
	private String promotionEndDate;
	
	private String issuer;
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