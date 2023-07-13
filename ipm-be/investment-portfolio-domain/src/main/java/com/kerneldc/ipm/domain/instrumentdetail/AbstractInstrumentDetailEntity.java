package com.kerneldc.ipm.domain.instrumentdetail;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import com.kerneldc.common.domain.AbstractPersistableEntity;
import com.kerneldc.ipm.domain.Instrument;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class AbstractInstrumentDetailEntity extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;

	@OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "instrument_id")
	@Setter(AccessLevel.NONE)
	protected Instrument instrument;

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
		setLogicalKeyHolder();
	}

}
