package com.kerneldc.ipm.repository.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstrumentStockRepositoryService extends AbstractRepositoryService<InstrumentStock, Long>{
	
	public InstrumentStockRepositoryService(JpaRepository<InstrumentStock, Long> instrumentStockRepository) {
		super(instrumentStockRepository);
	}

	@Override
	protected void handleEntity(InstrumentStock is) {
		LOGGER.info("is: {}", is);
		var i = is.getInstrument();
		i.setType(InstrumentTypeEnum.STOCK);
	}
	
}
