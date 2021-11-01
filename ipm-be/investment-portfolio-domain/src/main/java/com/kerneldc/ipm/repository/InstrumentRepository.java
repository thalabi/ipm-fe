package com.kerneldc.ipm.repository;

import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface InstrumentRepository extends BaseTableRepository<Instrument, Long> {
	
	List<Instrument> findByTickerAndExchange(String ticker, String exchange);
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT;
	}
	
}
