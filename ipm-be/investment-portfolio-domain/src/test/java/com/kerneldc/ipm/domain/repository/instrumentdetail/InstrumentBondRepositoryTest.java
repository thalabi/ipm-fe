package com.kerneldc.ipm.domain.repository.instrumentdetail;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.PaymentFrequencyEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentBond;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentBondRepository;

@DataJpaTest
class InstrumentBondRepositoryTest {

	@Autowired
	private InstrumentBondRepository instrumentBondRepository;
	
	@Test
	void testInstrumentBondRepositoryIsNotNull() {
		assertThat(instrumentBondRepository).isNotNull();
	}

	@Test
	void testInsertInstrumentBondRepository() {
		var i = new Instrument();
		i.setType(InstrumentTypeEnum.BOND);
		i.setTicker("CIBC 5.144 04/28/25");
		i.setName("USCORP CIBC 5.144 04/28/25");
		i.setCurrency(CurrencyEnum.USD);
		var ib = new InstrumentBond();
		ib.setInstrument(i);
		ib.setIssuer("USCORP, CAN. IMP. BK OF COMMERCE");
		ib.setCusip("13607LNF6");
		ib.setPrice(new BigDecimal("1000"));
		ib.setCoupon(new BigDecimal("5.144"));
		var dateFormatter1 = DateTimeFormatter.ofPattern("uuuu-MM-dd").withZone(ZoneId.systemDefault());

		ib.setIssueDate(offsetDateFromDateString("2023-04-28", dateFormatter1));
		ib.setMaturityDate(offsetDateFromDateString("2025-04-28", dateFormatter1));
		ib.setPaymentFrequency(PaymentFrequencyEnum.SEMIANNUALLY);
		ib.setNextPaymentDate(offsetDateFromDateString("2023-10-28", dateFormatter1));
		instrumentBondRepository.save(ib);
		System.out.println("Instrument: " + i);
		System.out.println("InstrumentBond: " + ib);
		assertThat(ib.getId()).isNotNull();
		assertThat(ib.getInstrument().getId()).isNotNull();
	}

	private OffsetDateTime offsetDateFromDateString(String date, DateTimeFormatter dateTimeFormatter) {
		return OffsetDateTime.ofInstant(
				LocalDate.parse(date, dateTimeFormatter).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()),
				ZoneId.systemDefault());
	}
}
