package com.kerneldc.portfolio.repository;

import java.util.List;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.domain.LogicalKeyHolder;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.portfolio.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.portfolio.domain.Price;

public interface PriceRepository extends BaseTableRepository<Price, Long> {
	
	List<Price> findByLogicalKeyHolder(LogicalKeyHolder logicalKeyHolder);
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PRICE;
	}
	
}
