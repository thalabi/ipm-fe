package com.kerneldc.portfolio.repository;

import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.portfolio.domain.HoldingPriceInterdayV;
import com.kerneldc.portfolio.domain.InvestmentPortfolioTableEnum;

public interface HoldingPriceInterdayVRepository extends BaseViewRepository<HoldingPriceInterdayV, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.HOLDING_PRICE_INTERDAY_V;
	}
	
}
