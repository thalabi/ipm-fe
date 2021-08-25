package com.kerneldc.portfolio.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.CurrencyEnum;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "instrument_seq", allocationSize = 1)
@Getter @Setter
public class Instrument extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String ticker;
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String exchange;
	@Enumerated(EnumType.STRING)
	@CsvBindByName
	private CurrencyEnum currency;
	@CsvBindByName
	private String name;

	public void setTicker(String ticker) {
		this.ticker = ticker;
		setLogicalKeyHolder();
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKey = concatLogicalKeyParts(ticker, exchange);
		getLogicalKeyHolder().setLogicalKey(logicalKey);
	}
}
