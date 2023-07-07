package com.kerneldc.ipm.repository;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Price;

public interface PriceRepository extends BaseTableRepository<Price, Long> {
	
	//Long deleteByIdIn(Collection<Long> idCollection);
	
	// Use customDeleteByIdIn instead of deleteByIdIn as the latter deletes one by one
//	@Modifying
//	@Query(value = "delete from price where id in :idCollection", nativeQuery = true)
//	void customDeleteByIdIn(Collection<Long> idCollection);

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PRICE;
	}
	
}
