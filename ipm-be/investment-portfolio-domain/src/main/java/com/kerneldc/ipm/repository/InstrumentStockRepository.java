package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InstrumentStock;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface InstrumentStockRepository extends BaseTableRepository<InstrumentStock, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_STOCK;
	}
	
}
