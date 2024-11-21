package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;

public interface InstrumentRepository extends BaseTableRepository<Instrument, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioEntityEnum.INSTRUMENT;
	}
	
}
