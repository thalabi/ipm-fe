package com.kerneldc.portfolio.repository;

import java.time.LocalDateTime;

import com.kerneldc.common.BaseTableRepository;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.portfolio.domain.InvestmentPortfolioTableEnum;
import com.kerneldc.portfolio.domain.Position;

public interface PositionRepository extends BaseTableRepository<Position, Long> {
	
	Long deleteByPositionSnapshotNot(LocalDateTime positionSnapshot);
	
	@Override
	default IEntityEnum canHandle() {
		return InvestmentPortfolioTableEnum.POSITION;
	}
	
}
