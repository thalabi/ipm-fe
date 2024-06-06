package com.kerneldc.common.domain;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

//import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class EntityHashAndEqualsTest {

	@Disabled // since the equals is commented out in AbstractPersistableEntity
	@Test
	void testEquals() {
		var td = LocalDateTime.of(2024,  04, 24, 5, 52);
		var s1 = new Sales();
		s1.setId(1l);
		s1.setTransactionDate(td);
		s1.setProduct("p");
		s1.setPrice(7d);
		s1.setPaymentType("pt");
		s1.setName("n");
		var s2 = new Sales();
		s1.setId(2l);
		s2.setTransactionDate(td);
		s2.setProduct("p");
		s2.setPrice(7d);
		s2.setPaymentType("pt");
		s2.setName("n");
		assertThat(s1.equals(s2), is(true));
	}

	@Disabled // since the hash is commented out in AbstractPersistableEntity
	@Test
	void testHash() {
		var td = LocalDateTime.of(2024,  04, 24, 5, 52);
		var s1 = new Sales();
		s1.setId(1l);
		s1.setTransactionDate(td);
		s1.setProduct("p");
		s1.setPrice(7d);
		s1.setPaymentType("pt");
		s1.setName("n");
		var s2 = new Sales();
		s1.setId(2l);
		s2.setTransactionDate(td);
		s2.setProduct("p");
		s2.setPrice(7d);
		s2.setPaymentType("pt");
		s2.setName("n");
		System.out.println(s1.hashCode() + ", " + s2.hashCode());
		assertThat(s1.hashCode(), equalTo(s2.hashCode()));
	}

}
