package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InstrumentByAccountV;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface InstrumentByAccountVRepository extends BaseViewRepository<InstrumentByAccountV, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_BY_ACCOUNT_V;
	}
	
}
