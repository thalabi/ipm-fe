package com.kerneldc.ipm.domain.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.DigestUtils;

import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.domain.repository.instrumentdetail.InstrumentInterestBearingRepositoryTest;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentInterestBearingRepository;
import com.kerneldc.ipm.repository.service.InstrumentInterestBearingRepositoryService;

@DataJpaTest
class InstrumentInterestBearingServiceTest {

	private InstrumentInterestBearingRepositoryService instrumentInterestBearingRepositoryService;
	
	@Autowired
	private InstrumentInterestBearingRepository instrumentInterestBearingRepository;
	@Autowired
	private JpaRepository<InstrumentInterestBearing, Long> jpaRepository;
	
	@BeforeEach
	void init() {
		instrumentInterestBearingRepositoryService = new InstrumentInterestBearingRepositoryService(instrumentInterestBearingRepository);
	}
	
	@Test
	void testInsertInstrumentInterestBearingtRepository_addInstrumentWithDuplicateTicker() {
		
		var iib = InstrumentInterestBearingRepositoryTest.moneyMarket();
		var iib2 = InstrumentInterestBearingRepositoryTest.moneyMarket();

		instrumentInterestBearingRepositoryService.save(iib);
		instrumentInterestBearingRepositoryService.save(iib2);
		
		var exception = assertThrows(DataIntegrityViolationException.class, () -> {
			// have to use JpaRepository instead of EntityManager since it wraps the ConstraintViolationException with DataIntegrityViolationException
			jpaRepository.flush();
	    });

		assertThat(exception).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void testInsertInstrumentInterestBearingtRepository_addInstrumentTestingMd5ThatHasZeroByte() {
		
		var iib = InstrumentInterestBearingRepositoryTest.chequing();

		instrumentInterestBearingRepositoryService.save(iib);
		
		// assert that the md5 of the name contains a zero byte
		var md5Digest = DigestUtils.md5Digest(iib.getInstrument().getName().getBytes());
		var nameMd5ContainsZero = List.of(ArrayUtils.toObject(md5Digest)).contains((byte) 0);
		assertThat(nameMd5ContainsZero).isTrue();
		
		// assert that the ticker which contains the md5 of the ticker has been stripped of the zero bytes
		var tickerBytes = iib.getInstrument().getTicker().getBytes();
		var tickerContainsZero = List.of(ArrayUtils.toObject(tickerBytes)).contains((byte) 0);
		assertThat(tickerContainsZero).isFalse();
	}

}
