package com.kerneldc.ipm.domain.instrumentdetail;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.ipm.domain.ExchangeEnum;
import com.opencsv.bean.CsvBindByName;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "inst_etf")
@SequenceGenerator(name = "default_seq_gen", sequenceName = "inst_etf_seq", allocationSize = 1)
@Getter @Setter
public class InstrumentEtf extends AbstractInstrumentDetailEntity implements IListedInstrumentDetail {
	
	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private ExchangeEnum exchange;

	public void setExchange(ExchangeEnum exchange) {
		this.exchange = exchange;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(instrument.getTicker(), exchange);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
