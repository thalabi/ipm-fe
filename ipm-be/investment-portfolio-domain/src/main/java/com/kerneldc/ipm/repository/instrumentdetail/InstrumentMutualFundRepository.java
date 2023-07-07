package com.kerneldc.ipm.repository.instrumentdetail;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMutualFund;
import com.kerneldc.ipm.repository.BaseInstrumentDetailRepository;

public interface InstrumentMutualFundRepository extends BaseInstrumentDetailRepository<InstrumentMutualFund, Long> {

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_MUTUAL_FUND;
	}
	
}
