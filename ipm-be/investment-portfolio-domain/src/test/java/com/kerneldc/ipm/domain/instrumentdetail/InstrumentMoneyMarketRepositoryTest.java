package com.kerneldc.ipm.domain.instrumentdetail;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentMoneyMarketRepository;

@DataJpaTest
class InstrumentMoneyMarketRepositoryTest {

	@Autowired
	private InstrumentMoneyMarketRepository instrumentMoneyMarketRepository;
	
	@Test
	void testInstrumentMoneyMarketRepositoryIsNotNull() {
		assertThat(instrumentMoneyMarketRepository).isNotNull();
	}

	@Test
	void testInsertInstrumentMoneyMarketRepository() {
		var i = new Instrument();
		i.setType(InstrumentTypeEnum.MONEY_MARKET);
		i.setTicker("CIB275");
		i.setName("Canadian Imperial Bank of Comm CIB275");
		i.setCurrency(CurrencyEnum.USD);
		var imm = new InstrumentMoneyMarket();
		imm.setInstrument(i);
		imm.setPrice(new BigDecimal("10"));
		instrumentMoneyMarketRepository.save(imm);
		System.out.println("Instrument: " + i);
		System.out.println("InstrumentMoneyMarket: " + imm);
		assertThat(imm.getId()).isNotNull();
		assertThat(imm.getInstrument().getId()).isNotNull();
	}

}
