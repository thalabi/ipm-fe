package com.kerneldc.ipm.repository.instrumentdetail;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentEtf;
import com.kerneldc.ipm.repository.BaseInstrumentDetailRepository;

public interface InstrumentEtfRepository extends BaseInstrumentDetailRepository<InstrumentEtf, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_ETF;
	}
	
}
