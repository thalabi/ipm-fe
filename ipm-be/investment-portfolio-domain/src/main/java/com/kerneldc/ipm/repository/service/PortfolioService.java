package com.kerneldc.ipm.repository.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.kerneldc.ipm.domain.Portfolio;

@Service
public class PortfolioService extends AbstractRepositoryService<Portfolio, Long> {

	public PortfolioService(JpaRepository<Portfolio, Long> holdingRepository) {
		super(holdingRepository);
	}

	@Override
	protected void handleEntity(Portfolio entity) {
		// nothing to do
	}
}
