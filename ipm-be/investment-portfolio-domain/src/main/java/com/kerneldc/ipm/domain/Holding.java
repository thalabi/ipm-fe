package com.kerneldc.ipm.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "holding_seq", allocationSize = 1)
@Getter @Setter
public class Holding extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName(column = "as_of_date")
	@CsvDate("uuuu-MM-dd'T'HH:mm:ss.SSSZ")
	@Setter(AccessLevel.NONE)
	private OffsetDateTime asOfDate;
	@Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "instrument_id")
	private Instrument instrument;
	@Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;
	
	@Transient
	@CsvBindByName
	private String ticker;
	@Transient
	@CsvBindByName
	private String exchange;

	@Transient
	@CsvBindByName
	private String institution;
	@Transient
	@CsvBindByName(column = "account_number")
	private String accountNumber;

	
	@CsvBindByName
	private Float quantity;

	public void setAsOfDate(OffsetDateTime asOfDate) {
		this.asOfDate = asOfDate;
		setLogicalKeyHolder();
	}
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
		setLogicalKeyHolder();
	}
	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKey = concatLogicalKeyParts(Objects.toString(asOfDate, StringUtils.EMPTY),
				(instrument != null ? instrument.getId().toString() : StringUtils.EMPTY),
				(portfolio != null ? portfolio.getId().toString() : StringUtils.EMPTY));
		getLogicalKeyHolder().setLogicalKey(logicalKey);
	}
}
