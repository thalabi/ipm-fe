package com.kerneldc.ipm.domain;

import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "position_seq", allocationSize = 1)
@Getter @Setter
public class Position extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName(column = "position_timestamp")
	@CsvDate(OFFSET_DATE_TIME_FORMAT)
	@Setter(AccessLevel.NONE)
	private OffsetDateTime positionSnapshot;
	@Setter(AccessLevel.NONE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "instrument_id")
	private Instrument instrument;
	@Setter(AccessLevel.NONE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;
	@CsvBindByName
	private Float quantity;
	
    @ManyToOne(optional = false)
    @JoinColumn(name = "price_id")
	private Price price;

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

	@Transient
	@CsvBindByName(column = "price_timestamp")
	@CsvDate(OFFSET_DATE_TIME_FORMAT)
	private OffsetDateTime priceTimestamp;

	public void setPositionSnapshot(OffsetDateTime positionSnapshot) {
		this.positionSnapshot = positionSnapshot;
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
		var logicalKeyHolder = LogicalKeyHolder.build(positionSnapshot,
				(instrument != null ? instrument.getId() : null),
				(portfolio != null ? portfolio.getId() : null));
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
