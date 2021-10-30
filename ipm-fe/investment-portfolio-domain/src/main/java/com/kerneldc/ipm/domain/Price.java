package com.kerneldc.ipm.domain;

import java.math.BigDecimal;
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
@SequenceGenerator(name = "default_seq_gen", sequenceName = "price_seq", allocationSize = 1)
@Getter @Setter
public class Price extends AbstractPersistableEntity {
	
	private static final long serialVersionUID = 1L;

	@Setter(AccessLevel.NONE)
    @ManyToOne
    @JoinColumn(name = "instrument_id")
	private Instrument instrument;
	private BigDecimal price;
	@Setter(AccessLevel.NONE)
	private OffsetDateTime priceTimestamp;
	// priceTimestampFromSource indicates if the priceTimestamp field was available from source or was replaced by the current timestamp
	private Boolean priceTimestampFromSource;

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
		setLogicalKeyHolder();
	}
	public void setPriceTimestamp(OffsetDateTime priceTimestamp) {
		this.priceTimestamp = priceTimestamp;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKey = concatLogicalKeyParts((instrument != null ? instrument.getId().toString() : StringUtils.EMPTY),
				Objects.toString(priceTimestamp, StringUtils.EMPTY)
				);
		getLogicalKeyHolder().setLogicalKey(logicalKey);
	}
}
