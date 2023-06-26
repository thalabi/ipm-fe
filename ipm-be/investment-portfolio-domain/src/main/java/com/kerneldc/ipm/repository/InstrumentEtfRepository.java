package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InstrumentEtf;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface InstrumentEtfRepository extends BaseTableRepository<InstrumentEtf, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_ETF;
	}
	
}
