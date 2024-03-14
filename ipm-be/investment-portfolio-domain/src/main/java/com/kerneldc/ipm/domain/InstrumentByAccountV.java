package com.kerneldc.ipm.domain;

import org.hibernate.annotations.Immutable;
import org.springframework.data.rest.core.annotation.Description;

import com.kerneldc.common.domain.AbstractImmutableEntity;
import com.opencsv.bean.CsvBindByName;

import jakarta.persistence.Entity;
import lombok.Getter;

@Entity(name = "instrument_by_account_v")
@Immutable
@Getter
public class InstrumentByAccountV extends AbstractImmutableEntity {
	
	@CsvBindByName(column = "ticker_exchange")
	@Description("columnDisplayOrder=1,title=Ticker,sortOrder=1,filterable=true,type=text") // sortDirection=desc is also available to specify direction
	private String ticker;
	@CsvBindByName(column = "instrument_name,filterable=true,type=text")
	@Description("columnDisplayOrder=2")
	private String instrumentName;
	@CsvBindByName
	@Description("columnDisplayOrder=3,title=Qty")
	private Float quantity;
	@CsvBindByName(column = "account_id")
	@Description("columnDisplayOrder=4,title=Acc ID,sortOrder=2,filterable=true,type=text")
	private String accountId;
	@CsvBindByName(column = "account_name")
	@Description("columnDisplayOrder=5,title=Acc Name,filterable=true,type=text")
	private String accountName;
}