package com.kerneldc.ipm.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;
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
	@CsvDate(OFFSET_DATE_TIME_FORMAT)
	@Setter(AccessLevel.NONE)
	private OffsetDateTime asOfDate;
	@Setter(AccessLevel.NONE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "instrument_id")
	private Instrument instrument;
	@Setter(AccessLevel.NONE)
    @ManyToOne(optional = false)
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
	private BigDecimal quantity;

	@Transient
	private IInstrumentDetail instrumentDetail;
	
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
		var logicalKeyHolder = LogicalKeyHolder.build(asOfDate,
				(instrument != null ? instrument.getId() : null),
				(portfolio != null ? portfolio.getId() : null));
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
