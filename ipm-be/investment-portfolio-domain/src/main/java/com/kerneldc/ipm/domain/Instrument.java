package com.kerneldc.ipm.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "instrument_seq", allocationSize = 1)
@Getter @Setter
public class Instrument extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private InstrumentTypeEnum type; 
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	@Size(min = 1, max = 16, message = "Ticker must be between 1 and 16 characters")
	private String ticker;
	@Enumerated(EnumType.STRING)
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	@NotNull
	private CurrencyEnum currency;
	@CsvBindByName
	private String name;
	@CsvBindByName
	private String notes;

	public void setType(InstrumentTypeEnum type) {
		this.type = type;
		setLogicalKeyHolder();
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
		setLogicalKeyHolder();
	}
	public void setCurrency(CurrencyEnum currency) {
		this.currency = currency;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(ticker, currency, type);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
