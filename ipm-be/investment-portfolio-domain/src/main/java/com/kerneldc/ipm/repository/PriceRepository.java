package com.kerneldc.ipm.repository;

import java.util.Collection;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.ipm.domain.Price;

public interface PriceRepository extends BaseTableRepository<Price, Long> {
	
//	@Modifying
//	@Query(value = """
//			delete from price where id in (select price_id from position where position_snapshot = :positionSnapshot)
//			"""
//			, nativeQuery = true)
//	void deleteByPositionSnapshot(LocalDateTime positionSnapshot);
	
	Long deleteByIdIn(Collection<Long> idCollection);

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PRICE;
	}
	
}
