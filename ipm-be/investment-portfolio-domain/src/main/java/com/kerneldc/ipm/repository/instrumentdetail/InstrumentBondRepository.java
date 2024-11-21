package com.kerneldc.ipm.repository.instrumentdetail;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentBond;
import com.kerneldc.ipm.repository.BaseInstrumentDetailRepository;

public interface InstrumentBondRepository extends BaseInstrumentDetailRepository<InstrumentBond, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioEntityEnum.INSTRUMENT_BOND;
	}
	
}
