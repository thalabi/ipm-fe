package com.kerneldc.common.search;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.kerneldc.common.AbstractBaseTest;
import com.kerneldc.common.domain.Sales;
import com.kerneldc.common.domain.SunshineList;
import com.kerneldc.common.repository.SalesRepository;
import com.kerneldc.common.repository.SunshineListRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class EntitySpecificationTest extends AbstractBaseTest {

	@Autowired
	private SalesRepository salesRepository;
	@Autowired
	private SunshineListRepository sunshineListRepository;
	@Autowired
	private EntityManager em;

	@Test
	void testProductEquals(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "product|equals|product1");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		assertThat(salesRepository.findById(1l).isPresent(), is(true));
		assertThat(salesRepository.findById(2l).isPresent(), is(true));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getProduct(), is("product1"));
	}
	@Test
	void testProductStartsWith(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "product|startsWith|p");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		assertThat(salesRepository.findById(1l).isPresent(), is(true));
		assertThat(salesRepository.findById(2l).isPresent(), is(true));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(2));
	}
	
	@Test
	void testPriceEquals(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "price|equals|100.01");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getPrice(), is(100.01d));
	}
	
	@Test
	void testPriceGreaterThan(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "price|greaterThan|100.01");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getPrice(), is(greaterThan(100.01d)));
	}
	@Test
	void testPriceGreaterThanOrEqual(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "price|greaterThanOrEqualTo|100");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(2));
		assertThat(salesList.get(0).getPrice(), is(greaterThanOrEqualTo(100d)));
		assertThat(salesList.get(1).getPrice(), is(greaterThanOrEqualTo(100d)));
	}
	@Test
	void testLatitudeEquals(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "latitude|equals|45.5019");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getLatitude(), is(45.5019f));
	}
	@Test
	void testLatitudeGreaterThan(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "latitude|greaterThan|45.5018");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getLatitude(), is(greaterThan(45.5018f)));
	}
	@Test
	void testTransactionDateEquals(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "transactionDate|dateIs|2024-02-16T11:04:29.000Z");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getTransactionDate(), is(LocalDateTime.of(2024,  02, 16, 11, 04, 29)));
	}
	@Test
	void testTransactionDateGreaterThan(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(Sales.class);
		var entitySpec = new EntitySpecification<Sales>(entityMetamodel, "transactionDate|dateAfter|2024-02-16T11:04:29.000Z");
		var s1 =createSales1();
		salesRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSales2();
		salesRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		var salesList = salesRepository.findAll(entitySpec);
		assertThat(salesList.size(), is(1));
		assertThat(salesList.get(0).getTransactionDate(), is(LocalDateTime.of(2024,  02, 21, 12, 39, 57)));
	}

	@Test
	void testSalaryEquals(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(SunshineList.class);
		var entitySpec = new EntitySpecification<SunshineList>(entityMetamodel, "salary|equals|100001.99");
		var s1 =createSunshineList1();
		sunshineListRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSunshineList2();
		sunshineListRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		assertThat(sunshineListRepository.findById(1l).isPresent(), is(true));
		assertThat(sunshineListRepository.findById(2l).isPresent(), is(true));
		
		var sunshineListList = sunshineListRepository.findAll(entitySpec);
		assertThat(sunshineListList.size(), is(1));
		//assertThat(sunshineListList.get(0).getSalary(), is(new BigDecimal("100001.99")));
		assertThat(sunshineListList.get(0).getSalary(), is(new BigDecimal("100001.99")));
	}

	@Test
	void testFirstNameAndLastName(TestInfo testInfo) {
		printTestName(testInfo);
		var entityMetamodel = em.getMetamodel().entity(SunshineList.class);
		var entitySpec = new EntitySpecification<SunshineList>(entityMetamodel, "firstName|equals|May,lastName|equals|Family");
		var s1 =createSunshineList1();
		sunshineListRepository.saveAndFlush(s1);
		assertThat(s1.getId(), is(1l));
		var s2 =createSunshineList2();
		sunshineListRepository.saveAndFlush(s2);
		assertThat(s2.getId(), is(2l));
		
		
		var sunshineListList = sunshineListRepository.findAll(entitySpec);
		assertThat(sunshineListList.size(), is(1));
	}

	private Sales createSales1() {
		var s1 = new Sales();
		s1.setProduct("product1");
		s1.setPrice(100.01d);
		s1.setLatitude(43.6532f);
		s1.setTransactionDate(LocalDateTime.of(2024,  02, 16, 11, 04, 29));
		return s1;
	}
	private Sales createSales2() {
		var s1 = new Sales();
		s1.setProduct("product2");
		s1.setPrice(200d);
		s1.setLatitude(45.5019f);
		s1.setTransactionDate(LocalDateTime.of(2024,  02, 21, 12, 39, 57));
		return s1;
	}
	private SunshineList createSunshineList1() {
		var sl = new SunshineList();
		sl.setSector("Edu");
		sl.setLastName("Family");
		sl.setFirstName("May");
		sl.setSalary(new BigDecimal("100001.99"));
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
