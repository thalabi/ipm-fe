package com.kerneldc.ipm.domain.instrumentdetail;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "inst_stock")
@SequenceGenerator(name = "default_seq_gen", sequenceName = "inst_stock_seq", allocationSize = 1)
@Getter @Setter
public class InstrumentStock extends AbstractInstrumentDetailEntity implements IListedInstrumentDetail {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private String exchange;


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
