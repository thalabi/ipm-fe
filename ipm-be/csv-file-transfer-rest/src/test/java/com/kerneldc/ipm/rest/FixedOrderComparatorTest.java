package com.kerneldc.ipm.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.junit.jupiter.api.Test;

class FixedOrderComparatorTest {

	@Test
	void test1() {
		String[] planets = {"Earth", "Mars", "Mercury", "Venus"};
		String[] expectedOrder = {"Mercury", "Venus", "Earth", "Mars"};
		var orderByDistanceFromSun = new FixedOrderComparator<String>(new String[]{ "Mercury", "Venus", "Earth", "Mars" });
		Arrays.sort(planets, orderByDistanceFromSun);
		assertThat(planets).isEqualTo(expectedOrder);
		
		// Mars is further away that Mercury from the Sun
		assertThat(orderByDistanceFromSun.compare("Mars", "Mercury")).isEqualTo(1);
		// Venus is closer to t he Sun than Earth
		assertThat(orderByDistanceFromSun.compare("Venus", "Earth")).isEqualTo(-1);
	}

}
