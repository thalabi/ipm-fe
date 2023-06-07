package com.kerneldc.ipm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.kerneldc.common.BaseViewRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.HoldingPriceInterdayV;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;

public interface HoldingPriceInterdayVRepository extends BaseViewRepository<HoldingPriceInterdayV, Long> {
	
	@Query(value = """
			select id, position_snapshot, market_value from (
			       select id, position_snapshot, market_value from holding_price_interday_v hpiv
			        where hpiv.position_snapshot in (
			                select max(hpiv1.position_snapshot) from holding_price_interday_v hpiv1 group by cast (hpiv1.position_snapshot as date))
			        order by position_snapshot desc limit :nPositionSnapshots
			       ) as t
			       order by position_snapshot
			"""
			, nativeQuery = true)
	List<HoldingPriceInterdayV> selectLastestNMarketValues(int nPositionSnapshots);
	
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.HOLDING_PRICE_INTERDAY_V;
	}
	
}
