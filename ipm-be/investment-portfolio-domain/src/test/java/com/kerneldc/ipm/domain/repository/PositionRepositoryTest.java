package com.kerneldc.ipm.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.HolderEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.Portfolio;
import com.kerneldc.ipm.domain.Position;
import com.kerneldc.ipm.domain.Price;
import com.kerneldc.ipm.repository.InstrumentRepository;
import com.kerneldc.ipm.repository.PortfolioRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.repository.PriceRepository;

@DataJpaTest
class PositionRepositoryTest {

	@Autowired
	private PositionRepository positionRepository;
	@Autowired
	private PortfolioRepository portfolioRepository;
	@Autowired
	private InstrumentRepository instrumentRepository;
	@Autowired
	private PriceRepository priceRepository;
	
	@Test
	void testPortfolioRepositoryIsNotNull() {
		assertThat(positionRepository).isNotNull();
		assertThat(portfolioRepository).isNotNull();
		assertThat(instrumentRepository).isNotNull();
		assertThat(priceRepository).isNotNull();
	}

	@Test
	void testEquityReport() {
		var por = new Portfolio();
		por.setFinancialInstitution(FinancialInstitutionEnum.TD);
		por.setHolder(HolderEnum.JOINT);
		por.setPortfolioId("426YF8F");
		por.setName("Joint Account Tarif-May USD");
		por.setCurrency(CurrencyEnum.USD);
		por.setLogicallyDeleted(false);
		portfolioRepository.saveAndFlush(por);
		
		var i = new Instrument();
		i.setTicker("T");
		i.setCurrency(CurrencyEnum.USD);
		i.setName("AT&T Inc.");
		i.setType(InstrumentTypeEnum.STOCK);
		instrumentRepository.saveAndFlush(i);
		
		var pri = new Price();
		pri.setInstrument(i);
		pri.setPrice(BigDecimal.ONE);
		pri.setPriceTimestamp(OffsetDateTime.now());
		pri.setPriceTimestampFromSource(true);
		priceRepository.saveAndFlush(pri);
		
		var pos = new Position();
		pos.setPositionSnapshot(OffsetDateTime.now());
		pos.setInstrument(i);
		pos.setPortfolio(por);
		pos.setQuantity(new BigDecimal("4825.0000"));
		pos.setPrice(pri);
		positionRepository.saveAndFlush(pos);
		
		var equityReportRows = positionRepository.equityReport();
		
		assertThat(equityReportRows).isNotNull();
		assertThat(equityReportRows).hasSize(1);
		var er = equityReportRows.get(0);
		assertThat(er.getFinancialInstitutionNumber()).isNotNull();
		assertThat(er.getHolder()).isNotNull();
//		System.out.println(er.getPriceTimestamp().toLocalDateTime());
//		System.out.println(er.getPriceTimestamp().toLocalDate());
	}
}
