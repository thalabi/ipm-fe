package com.kerneldc.ipm.domain.instrumentdetail;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;

import com.kerneldc.common.domain.LogicalKeyHolder;
import com.opencsv.bean.CsvBindByName;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "inst_money_market")
@SequenceGenerator(name = "default_seq_gen", sequenceName = "inst_money_market_seq", allocationSize = 1)
@Getter @Setter
public class InstrumentMoneyMarket extends AbstractInstrumentDetailEntity implements IFixedPriceInstrumentDetail {
	
	private static final long serialVersionUID = 1L;

	@CsvBindByName
	@Setter(AccessLevel.NONE)
	private BigDecimal price;


	public void setPrice(BigDecimal price) {
		this.price = price;
		setLogicalKeyHolder();
	}
	
	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(instrument.getTicker(), price);
		setLogicalKeyHolder(logicalKeyHolder);
	}
}
