package com.kerneldc.ipm.repository.instrumentdetail;

import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMoneyMarket;
import com.kerneldc.ipm.repository.BaseInstrumentDetailRepository;

public interface InstrumentMoneyMarketRepository extends BaseInstrumentDetailRepository<InstrumentMoneyMarket, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.INSTRUMENT_MONEY_MARKET;
	}
	
}
