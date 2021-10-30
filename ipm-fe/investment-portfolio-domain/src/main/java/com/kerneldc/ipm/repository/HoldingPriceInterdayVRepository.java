package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.HoldingPriceInterdayV;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface HoldingPriceInterdayVRepository extends BaseViewRepository<HoldingPriceInterdayV, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.HOLDING_PRICE_INTERDAY_V;
	}
	
}
