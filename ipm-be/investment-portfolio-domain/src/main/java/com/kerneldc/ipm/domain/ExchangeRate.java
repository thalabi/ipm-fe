package com.kerneldc.ipm.domain;



import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.common.enums.CurrencyEnum;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "exchange_rate_seq", allocationSize = 1)
@Getter @Setter
public class ExchangeRate extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName(column = "as_of_date")
	@CsvDate(OFFSET_DATE_TIME_FORMAT)
	@Setter(AccessLevel.NONE)
	private OffsetDateTime asOfDate;
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

	public void setAsOfDate(OffsetDateTime asOfDate) {
		this.asOfDate = asOfDate;
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
		var logicalKeyHolder = LogicalKeyHolder.build(asOfDate,	(fromCurrency != null ? fromCurrency.toString() : StringUtils.EMPTY), (toCurrency != null ? toCurrency.toString() : StringUtils.EMPTY));
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
