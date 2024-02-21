package com.kerneldc.common.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.kerneldc.common.AbstractBaseTest;
import com.kerneldc.common.domain.SunshineList;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class SunshineListRepositoryAndJdbcTest extends AbstractBaseTest {
	
	@Autowired
	private SunshineListRepository sunshineListRepository;

	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	private static final String INSERT_SQL_1 = """
			INSERT INTO sunshine_list (id, lk, version, sector, last_name, first_name, salary, benefits, employer, job_title, calendar_year)
			VALUES(1, 'lk1', 0, '', 'Halabi', 'May', 100.01, 0, '', '', 1996)
			""";
	private static final String INSERT_SQL_2 = """
			INSERT INTO sunshine_list (id, lk, version, sector, last_name, first_name, salary, benefits, employer, job_title, calendar_year) 
			VALUES(1, 'lk1', 0, '', 'Halabi', 'May', 100, 0, '', '', 1996)
			""";
	@Test
	void testSelectById(TestInfo testInfo) {
		printTestName(testInfo);
		jdbcTemplate.update(INSERT_SQL_1);
		var namedParameters = new MapSqlParameterSource().addValue("id", 1);
		var id = namedParameterJdbcTemplate.queryForObject(
				  "select id from sunshine_list where id = :id", namedParameters, Long.class);
		System.out.println(id);
		assertThat(id, is(1l));
	}
	@Test
	void testSelectBySalaryDoesntWork(TestInfo testInfo) {
		printTestName(testInfo);
		jdbcTemplate.update(INSERT_SQL_1);
		var namedParameters = new MapSqlParameterSource().addValue("salary", 100.01);
		var salary = namedParameterJdbcTemplate.queryForObject(
				  "select salary from sunshine_list where salary = :salary", namedParameters, BigDecimal.class);
		System.out.println(salary);
		assertThat(salary, is(new BigDecimal("100.0100")));
}
	@Test
	void testSelectBySalaryWorks(TestInfo testInfo) {
		printTestName(testInfo);
		jdbcTemplate.update(INSERT_SQL_2);
		var namedParameters = new MapSqlParameterSource().addValue("salary", 100);
		var salary = namedParameterJdbcTemplate.queryForObject(
				  "select salary from sunshine_list where salary = :salary", namedParameters, BigDecimal.class);
		System.out.println(salary);
		assertThat(salary, is(new BigDecimal("100.0000")));
	}
	@Test
	void testUsingFindBySalary(TestInfo testInfo) {
		printTestName(testInfo);
		var sl1 = createSunshineList1();
		//System.out.println(sl);
		sunshineListRepository.save(sl1);
		var subshineListList = sunshineListRepository.findBySalary(new BigDecimal("100001.01"));
		assertThat(subshineListList.size(), is(1));
	}
	@Test
	void testUsingFindByBenefits(TestInfo testInfo) {
		printTestName(testInfo);
		var sl2 = createSunshineList2();
		//System.out.println(sl);
		sunshineListRepository.save(sl2);
		var subshineListList = sunshineListRepository.findByBenefits(new BigDecimal("1000.1234"));
		assertThat(subshineListList.size(), is(1));
	}
//
//	@Test
//	void testSpecificationSearchWithCriteria3(TestInfo testInfo) {
//		printTestName(testInfo);
//		var sl1 = createSunshineList1();
//		//System.out.println(sl);
//		sunshineListRepository.save(sl1);
//		var sl2 = createSunshineList2();
//		//System.out.println(sl);
//		sunshineListRepository.save(sl2);
//
//		//Specification<SunshineList> = (root, query, cb) -> cb.eq
//		Specification<SunshineList> sunshineListSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("calendarYear"), "2023");
//        List<SunshineList> list = sunshineListRepository.findAll(sunshineListSpec);
//        assertThat(list.size(), is(1));
//        assertThat(list.get(0).getId(), is(2l));
//        
//	}
//
//	
	private SunshineList createSunshineList1() {
		var sl = new SunshineList();
		sl.setSector("Edu");
		sl.setLastName("Family");
		sl.setFirstName("May");
		sl.setSalary(new BigDecimal("100001.01"));
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
		sl.setBenefits(new BigDecimal("1000.1234"));
		sl.setEmployer("MIT");
		sl.setJobTitle("Accountant");
		sl.setCalendarYear((short) 2023);
		return sl;
	}
}
