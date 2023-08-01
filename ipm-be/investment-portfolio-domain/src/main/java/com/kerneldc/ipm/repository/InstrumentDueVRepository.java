package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InstrumentDueV;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface InstrumentDueVRepository extends BaseViewRepository<InstrumentDueV, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_DUE_V;
	}
	
}
