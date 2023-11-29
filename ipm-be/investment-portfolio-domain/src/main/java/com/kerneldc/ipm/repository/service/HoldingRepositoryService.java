package com.kerneldc.ipm.repository.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.kerneldc.ipm.domain.Holding;

@Service
public class HoldingRepositoryService extends AbstractRepositoryService<Holding, Long> {

	public HoldingRepositoryService(JpaRepository<Holding, Long> holdingRepository) {
		super(holdingRepository);
	}
	
	@Override
	protected void handleEntity(Holding entity) {
		// nothing to handle
	}

}
