package com.kerneldc.ipm.domain;

import jakarta.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.springframework.data.rest.core.annotation.Description;

import com.kerneldc.common.domain.AbstractImmutableEntity;
import com.opencsv.bean.CsvBindByName;

import lombok.Getter;

@Entity(name = "instrument_by_account_v")
@Immutable
@Getter
public class InstrumentByAccountV extends AbstractImmutableEntity {
	
	@CsvBindByName(column = "ticker_exchange")
	@Description("columnDisplayOrder=1,title=Ticker,sortOrder=1") // sortDirection=desc is also available to specify direction
	private String tickerExchange;
	@CsvBindByName(column = "instrument_name")
	@Description("columnDisplayOrder=2")
	private String instrumentName;
	@CsvBindByName
	@Description("columnDisplayOrder=3,title=Qty")
	private Float quantity;
	@CsvBindByName(column = "account_number")
	@Description("columnDisplayOrder=4,title=Acc No,sortOrder=2")
	private String accountNumber;
	@CsvBindByName(column = "account_name")
	@Description("columnDisplayOrder=5,title=Acc Name")
	private String accountName;
}