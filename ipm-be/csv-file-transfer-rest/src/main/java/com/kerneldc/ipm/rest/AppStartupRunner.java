package com.kerneldc.ipm.rest;

import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import com.kerneldc.ipm.batch.InstrumentDueNotificationService;
import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentStock;
import com.kerneldc.ipm.repository.HoldingRepository;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentMutualFundRepository;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentStockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Component
@RequiredArgsConstructor
@Slf4j
public class AppStartupRunner implements ApplicationRunner {

	private final InstrumentRepository instrumentRepository;
	private final InstrumentStockRepository instrumentStockRepository;
	private final InstrumentMutualFundRepository instrumentMutualFundRepository;
	private final HoldingRepository holdingRepository;
	private final InstrumentDueNotificationService instrumentDueNotificationService;
	@Override
	public void run(ApplicationArguments args) throws Exception {
		LOGGER.info("App started");

		instrumentDueNotificationService.checkDueDate();
		

		
//		LOGGER.info("trace 0");
//		var ids = holdingRepository.findLatestAsOfDateHoldingIds();
//		var holdings = holdingRepository.findByIdIn(ids);
//		LOGGER.info("trace 0 end");
		
//		Optional<Instrument> iOptional = instrumentRepository.findById(1l);
//		iOptional.ifPresent(i -> {
//			LOGGER.info("trace 1");
//			var instrumentStockList0 = instrumentStockRepository.findByInstrumentIn(List.of(i));
//			LOGGER.info("instrumentStockList0.size(): {}", instrumentStockList0.size());
//		});


//		LOGGER.info("trace 1");
//		var instrumentStockList1 = instrumentStockRepository.findByInstrumentIdIn(List.of(1l,2l));
//		for (IInstrumentDetail instrumentStock: instrumentStockList1) {
//			LOGGER.info(instrumentStock.toString());
//		}
//		LOGGER.info("trace 2");
//		var instrumentStockList2 = instrumentStockRepository.findByInstrumentIdIn(List.of(1l,2l));
//		for (IInstrumentDetail iInstrumentDetail: instrumentStockList2) {
//			var instrumentStock = (InstrumentStock) iInstrumentDetail;
//			LOGGER.info(instrumentStock.getId().toString());
//		}
//		LOGGER.info("trace 3");
//		Optional<InstrumentStock> isOptional = instrumentStockRepository.findById(88l);
//		isOptional.ifPresent(is -> {
//			LOGGER.info(is.toString());
//		});
		//version2Inserts();
		//checkDeleteAllFromInstrument();
		//checkDeleteAll();
		//updateInstrumentViaInstrumentStock();
	}
	
	private void updateInstrumentViaInstrumentStock() {
		Optional<InstrumentStock> isOptional = instrumentStockRepository.findById(88l);
		isOptional.ifPresent(is -> {
			LOGGER.info(is.toString());
			is.setExchange(is.getExchange()+"+");
			var i = is.getInstrument();
			i.setName(i.getName() + "+");
			instrumentStockRepository.save(is);
			LOGGER.info(is.toString());
		});
	}

	private void checkDeleteAll() {
		instrumentStockRepository.deleteAll();
		instrumentMutualFundRepository.deleteAll();
	}

	private void version2Inserts() {
		instrumentRepository.deleteAll();
		var is1 = new InstrumentStock();
		var i1 = new Instrument();
		is1.setInstrument(i1);
		i1.setType(InstrumentTypeEnum.STOCK);
		i1.setName("BCE Inc");
		i1.setTicker("BCE");
		i1.setCurrency(CurrencyEnum.CAD);
		is1.setExchange("TSE");
		instrumentRepository.save(i1);
		instrumentStockRepository.save(is1);
		var instrumentList = instrumentRepository.findAll();
		LOGGER.info("instrumentList.size(): {}", instrumentList.size());
		for (Instrument i: instrumentList) {
			LOGGER.info(i.toString());
		}
		
		var instrumentStockList = instrumentStockRepository.findAll();
		LOGGER.info("instrumentStockList.size(): {}", instrumentStockList.size());
		for (InstrumentStock is: instrumentStockList) {
			LOGGER.info(is.toString());
		}
		
	}
}
