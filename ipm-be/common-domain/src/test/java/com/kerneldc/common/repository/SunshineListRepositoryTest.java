package com.kerneldc.common.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.kerneldc.common.AbstractBaseTest;
import com.kerneldc.common.domain.SunshineList;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class SunshineListRepositoryTest extends AbstractBaseTest {
	
	@Autowired
	private SunshineListRepository sunshineListRepository;

	@Test
	void testFindById(TestInfo testInfo) {
		printTestName(testInfo);
		var sl1 = createSunshineList1();
		sunshineListRepository.save(sl1);
		System.out.println(sl1);
		var sl2 = createSunshineList2();
		sunshineListRepository.save(sl2);
		System.out.println(sl2);

		Optional<SunshineList> slSavedOptional1 = sunshineListRepository.findById(1l);
		slSavedOptional1.isPresent();
		assertThat(slSavedOptional1.isPresent(), is(true));
		assertThat(slSavedOptional1.get().getId(), is(1l));
		Optional<SunshineList> slSavedOptional2 = sunshineListRepository.findById(2l);
		slSavedOptional2.isPresent();
		assertThat(slSavedOptional2.isPresent(), is(true));
		assertThat(slSavedOptional2.get().getId(), is(2l));
	}

	@Test
	void testSpecificationSearchWithCriteria3(TestInfo testInfo) {
		printTestName(testInfo);
		var sl1 = createSunshineList1();
		//System.out.println(sl);
		sunshineListRepository.save(sl1);
		var sl2 = createSunshineList2();
		//System.out.println(sl);
		sunshineListRepository.save(sl2);

		//Specification<SunshineList> = (root, query, cb) -> cb.eq
		Specification<SunshineList> sunshineListSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("calendarYear"), "2023");
        List<SunshineList> list = sunshineListRepository.findAll(sunshineListSpec);
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getId(), is(2l));
        
	}

//	@Test
//	void testFindByCalendarYear(TestInfo testInfo) {
//		printTestName(testInfo);
//		var sl1 = createSunshineList1();
//		//System.out.println(sl);
//		sunshineListRepository.save(sl1);
//		var sl2 = createSunshineList2();
//		//System.out.println(sl);
//		sunshineListRepository.save(sl2);
//
//		//Specification<SunshineList> = (root, query, cb) -> cb.eq
//        List<SunshineList> list = sunshineListRepository.findByCalendarYear((short) 2023);
//        assertThat(list.size(), is(1));
//        assertThat(list.get(0).getId(), is(2l));
//        
//	}
	
	private SunshineList createSunshineList1() {
		var sl = new SunshineList();
		sl.setSector("Edu");
		sl.setLastName("Family");
		sl.setFirstName("May");
		sl.setSalary(new BigDecimal("100001"));
		sl.setBenefits(BigDecimal.ZERO);
		sl.setEmployer("TDSB");
		sl.setJobTitle("Principal");
		sl.setCalendarYear((short) 2024);
		return sl;
	}

	private SunshineList createSunshineList2() {
		var sl = new SunshineList();
		sl.setSector("IT");
		sl.setLastName("Family");
		sl.setFirstName("Hamza");
		sl.setSalary(new BigDecimal("77000"));
		sl.setBenefits(BigDecimal.ZERO);
		sl.setEmployer("MIT");
		sl.setJobTitle("Accountant");
		sl.setCalendarYear((short) 2023);
		return sl;
	}
}
