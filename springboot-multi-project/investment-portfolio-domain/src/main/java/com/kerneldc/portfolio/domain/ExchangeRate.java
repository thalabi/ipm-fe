package com.kerneldc.portfolio.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.enums.CurrencyEnum;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "exchange_rate_seq", allocationSize = 1)
@Getter @Setter
public class ExchangeRate extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private OffsetDateTime date;
	@CsvBindByName(column = "from_currency")
	@Setter(AccessLevel.NONE)
	@Enumerated(EnumType.STRING)
	private CurrencyEnum fromCurrency;
	@CsvBindByName(column = "to_currency")
	@Setter(AccessLevel.NONE)
	@Enumerated(EnumType.STRING)
	private CurrencyEnum toCurrency;
	@CsvBindByName
	private Double rate;

	public void setDate(OffsetDateTime date) {
		this.date = date;
		setLogicalKeyHolder();
	}
	public void setFromCurrency(CurrencyEnum fromCurrency) {
		this.fromCurrency = fromCurrency;
		setLogicalKeyHolder();
	}
	public void setToCurrency(CurrencyEnum toCurrency) {
		this.toCurrency = toCurrency;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKey = concatLogicalKeyParts(Objects.toString(date, StringUtils.EMPTY),
				Objects.toString(fromCurrency, StringUtils.EMPTY),
				Objects.toString(toCurrency, StringUtils.EMPTY));
		getLogicalKeyHolder().setLogicalKey(logicalKey);
	}
}
