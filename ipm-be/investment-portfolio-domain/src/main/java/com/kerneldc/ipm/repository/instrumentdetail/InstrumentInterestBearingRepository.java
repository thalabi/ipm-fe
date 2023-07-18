package com.kerneldc.ipm.repository.instrumentdetail;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.repository.BaseInstrumentDetailRepository;

public interface InstrumentInterestBearingRepository extends BaseInstrumentDetailRepository<InstrumentInterestBearing, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_INTEREST_BEARING;
	}
	
}
