package com.kerneldc.ipm.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;
import com.kerneldc.ipm.domain.Position;

public interface PositionRepository extends BaseTableRepository<Position, Long> {
	
	Long deleteByPositionSnapshotNot(OffsetDateTime positionSnapshot);
	
	@Query(value = """
			select distinct cast(position_snapshot as varchar) as positionSnapshot from position order by positionSnapshot
			"""
			, nativeQuery = true)
	List<PositionSnapshot> selectAllPositionSnapshots();
	
	@EntityGraph(attributePaths = { "instrument", "portfolio", "price" })
	List<Position> findByPositionSnapshot(OffsetDateTime positionSnapshot);
	
	@Query(value = """
			select distinct price_id from position where price_id in :priceIdCollection
			"""
			, nativeQuery = true)
	List<Long> selectExistingPriceIds(Collection<Long> priceIdCollection);

	@Query(value = """
			select por.financial_institution financialInstitutionNumber, por.holder, por.portfolio_id portfolioId, por.name portfolioName, por.currency,
			       i.name instrumentName, i.ticker, i.type instrumentType,
			       pos.quantity, 
			       pri.price, cast(pri.price_timestamp as varchar) priceTimestamp, pri.price_timestamp_from_source priceTimestampFromSource,
			       pos.quantity * pri.price marketValue
			  from position pos
			  join portfolio por on pos.portfolio_id = por.id  
			  join instrument i on pos.instrument_id = i.id
			  --left outer join inst_stock ist on i.id = ist.instrument_id 
			  left outer join inst_etf iet on i.id = iet.instrument_id
			  join price pri on pos.price_id = pri.id
			 where position_snapshot = (select max(position_snapshot) from position)
			   and por.logically_deleted = false
			   and i.type in ('STOCK', 'ETF', 'CASH')
			 order by por.financial_institution, por.holder, por.portfolio_id, i.name
 	""", nativeQuery = true)
	List<IEquityReport> equityReport();	

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioEntityEnum.POSITION;
	}
	
}
