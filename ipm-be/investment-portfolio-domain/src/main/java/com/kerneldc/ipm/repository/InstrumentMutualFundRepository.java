package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InstrumentMutualFund;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface InstrumentMutualFundRepository extends BaseTableRepository<InstrumentMutualFund, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_MUTUAL_FUND;
	}
	
}
