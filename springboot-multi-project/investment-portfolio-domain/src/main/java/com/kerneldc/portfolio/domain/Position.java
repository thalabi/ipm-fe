package com.kerneldc.portfolio.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang3.StringUtils;

import com.kerneldc.common.domain.AbstractPersistableEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "default_seq_gen", sequenceName = "position_seq", allocationSize = 1)
@Getter @Setter
public class Position extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@Setter(AccessLevel.NONE)
	private OffsetDateTime positionSnapshot;
	@Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "instrument_id")
	private Instrument instrument;
	@Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;
	private Float quantity;
	
    @ManyToOne
    @JoinColumn(name = "price_id")
	private Price price;

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
		var logicalKey = concatLogicalKeyParts(Objects.toString(positionSnapshot, StringUtils.EMPTY),
				(instrument != null ? instrument.getId().toString() : StringUtils.EMPTY),
				(portfolio != null ? portfolio.getId().toString() : StringUtils.EMPTY));
		getLogicalKeyHolder().setLogicalKey(logicalKey);
	}
}
