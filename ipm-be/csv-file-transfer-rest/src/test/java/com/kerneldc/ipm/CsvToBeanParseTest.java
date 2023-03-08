package com.kerneldc.ipm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.opencsv.bean.CsvToBeanBuilder;

class CsvToBeanParseTest {

	@Test
	void test1() {
		var csvData = new String("""
				firstName
				tarif
				""");
		List<Bean1> beans = new CsvToBeanBuilder<Bean1>(new StringReader(csvData))
			       .withType(Bean1.class).build().parse();
		
		System.out.println(beans);
		assertThat(beans.get(0).getFirstName(), equalTo("tarif"));
	}
	@Test
	void test2_fails() {
		var csvData = new String("""
				first Name
				may
				""");
		List<Bean1> beans = new CsvToBeanBuilder<Bean1>(new StringReader(csvData))
			       .withType(Bean1.class).build().parse();
		
		System.out.println(beans);
		assertThat(beans.get(0).getFirstName(), not(equalTo("may")));
	}
	@Test
	void test3() {
		var csvData = new String("""
				firstname
				layla
				""");
		List<Bean1> beans = new CsvToBeanBuilder<Bean1>(new StringReader(csvData))
			       .withType(Bean1.class).build().parse();
		
		System.out.println(beans);
		assertThat(beans.get(0).getFirstName(), equalTo("layla"));
	}
}
