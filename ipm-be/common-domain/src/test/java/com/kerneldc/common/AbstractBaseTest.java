package com.kerneldc.common;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.TestInfo;

public abstract class AbstractBaseTest {

	private static final String NEW_LINE = System.getProperty("line.separator");
	
	protected static void printTestName(TestInfo testInfo) {
		System.out.println(StringUtils.repeat("=", 80) + NEW_LINE + testInfo.getDisplayName() + NEW_LINE + StringUtils.repeat("=", 80));
	}

}
