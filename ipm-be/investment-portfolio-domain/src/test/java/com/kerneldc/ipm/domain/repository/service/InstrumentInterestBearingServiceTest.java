package com.kerneldc.ipm.domain.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;
import com.kerneldc.ipm.domain.repository.instrumentdetail.InstrumentInterestBearingRepositoryTest;
import com.kerneldc.ipm.repository.instrumentdetail.InstrumentInterestBearingRepository;
import com.kerneldc.ipm.repository.service.InstrumentInterestBearingService;

@DataJpaTest
class InstrumentInterestBearingServiceTest {

	private InstrumentInterestBearingService instrumentInterestBearingService;
	
	@Autowired
	private InstrumentInterestBearingRepository instrumentInterestBearingRepository;
	@Autowired
	private JpaRepository<InstrumentInterestBearing, Long> jpaRepository;
	
	private static boolean isFixtureInitialized;
	
	@BeforeEach
	void init() {
		if (! /* not */ isFixtureInitialized) {
			// can not initialize fixture in a @BeforeAll method because the method has to be static and hence can't use @Autowired fields
			instrumentInterestBearingService = new InstrumentInterestBearingService(instrumentInterestBearingRepository);
			isFixtureInitialized = true;
		}
	}
	
	@Test
	void testInsertInstrumentInterestBearingtRepository_addInstrumentWithDuplicateTicker() {
		
		var iib = InstrumentInterestBearingRepositoryTest.moneyMarket();
		var iib2 = InstrumentInterestBearingRepositoryTest.moneyMarket();

		instrumentInterestBearingService.save(iib);
		instrumentInterestBearingService.save(iib2);
		
		var exception = assertThrows(DataIntegrityViolationException.class, () -> {
			// have to use JpaRepository instead of EntityManager since it wraps the ConstraintViolationException with DataIntegrityViolationException
			jpaRepository.flush();
	    });

		assertThat(exception).isInstanceOf(DataIntegrityViolationException.class);
	}


}
