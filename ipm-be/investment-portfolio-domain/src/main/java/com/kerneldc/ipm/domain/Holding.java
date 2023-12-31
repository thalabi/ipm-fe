package com.kerneldc.ipm.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.ipm.domain.instrumentdetail.IInstrumentDetail;
import com.kerneldc.ipm.domain.listener.FixedIncomeListener;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "holding_seq", allocationSize = 1)
@EntityListeners(FixedIncomeListener.class)
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
	@CsvBindByName
	private BigDecimal quantity;
	
	@Transient
	@CsvBindByName
	private String ticker;
	@Transient
	@CsvBindByName
	private ExchangeEnum exchange;

	@Transient
	@CsvBindByName
	private FinancialInstitutionEnum financialInstitution;
	@Transient
	@CsvBindByName(column = "account_number")
	private String accountNumber;

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
