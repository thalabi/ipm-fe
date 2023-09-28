package com.kerneldc.ipm.domain.instrumentdetail;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "inst_etf")
@SequenceGenerator(name = "default_seq_gen", sequenceName = "inst_etf_seq", allocationSize = 1)
@Getter @Setter
public class InstrumentEtf extends AbstractInstrumentDetailEntity implements IListedInstrumentDetail {
	
	private static final long serialVersionUID = 1L;

//	@OneToOne(cascade = CascadeType.ALL, optional = false)
//    @JoinColumn(name = "instrument_id")
//	@Setter(AccessLevel.NONE)
//    private Instrument instrument;
	
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String exchange;

//	public void setInstrument(Instrument instrument) {
//		this.instrument = instrument;
//		setLogicalKeyHolder();
//	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(instrument.getTicker(), exchange);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
