package com.kerneldc.ipm.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.ipm.domain.InvestmentPortfolioTableEnum;
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

	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.POSITION;
	}
	
}
