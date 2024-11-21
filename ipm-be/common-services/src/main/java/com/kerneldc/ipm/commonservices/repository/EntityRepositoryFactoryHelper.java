package com.kerneldc.ipm.commonservices.repository;

import org.springframework.stereotype.Service;

import com.kerneldc.common.enums.UploadTableEnum;
import com.kerneldc.common.repository.SunshineListRepository;
import com.kerneldc.ipm.domain.InvestmentPortfolioEntityEnum;
import com.kerneldc.ipm.repository.HoldingPriceInterdayVRepository;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PriceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EntityRepositoryFactoryHelper {

	private final EntityRepositoryFactory<?, ?> entityRepositoryFactory;
	
	public HoldingRepository getHoldingRepository() {
		return (HoldingRepository)entityRepositoryFactory.getRepository(InvestmentPortfolioEntityEnum.HOLDING);
	}
	public PositionRepository getPositionRepository() {
		return (PositionRepository)entityRepositoryFactory.getRepository(InvestmentPortfolioEntityEnum.POSITION);
	}
	public PriceRepository getPriceRepository() {
		return (PriceRepository)entityRepositoryFactory.getRepository(InvestmentPortfolioEntityEnum.PRICE);
	}
	public HoldingPriceInterdayVRepository getHoldingPriceInterdayVRepository() {
		return (HoldingPriceInterdayVRepository)entityRepositoryFactory.getRepository(InvestmentPortfolioEntityEnum.HOLDING_PRICE_INTERDAY_V);
	}

	
	public SunshineListRepository getSunshineListRepository() {
		return (SunshineListRepository)entityRepositoryFactory.getRepository(UploadTableEnum.SUNSHINE_LIST);
	}

}
