package com.kerneldc.portfolio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.portfolio.domain.Holding;
import com.kerneldc.portfolio.domain.InvestmentPortfolioTableEnum;

public interface HoldingRepository extends BaseTableRepository<Holding, Long> {
	
	List<Holding> findByAsOfDate(LocalDate asOfDate);
	
	@Query(value =
			"select * from holding h2 where (h2.as_of_date, h2.instrument_id, h2.portfolio_id) in (\r\n"
			+ "		select max(as_of_date) as_of_date, instrument_id, portfolio_id from holding h group by instrument_id, portfolio_id\r\n"
			+ ")", nativeQuery = true)
	List<Holding> findLatestAsOfDateHoldings();
	
	@Query(value = "select h.id, h.as_of_date asOfDate, h.instrument_id instrumentId, i.ticker, i.exchange, i.currency, i.name, h.quantity from holding h join instrument i on h.instrument_id = i.id where h.portfolio_id = :portfolioId order by h.instrument_id, h.as_of_date", nativeQuery = true)
	List<HoldingDetail> findByPortfolioId(Long portfolioId);

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.HOLDING;
	}
}
