package com.kerneldc.ipm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

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

	@Query(value = """
		with latest_timestamp_keys as (
			select p.instrument_id , max(p.price_timestamp) price_timestamp
			  from price p group by p.instrument_id)
		select p2.*
		  from price p2
		 where (p2.instrument_id, p2.price_timestamp) in (select lt.instrument_id, lt.price_timestamp
															from latest_timestamp_keys lt)
			""", nativeQuery = true)
	List<Price> findLatestPriceList();
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.PRICE;
	}
	
}
