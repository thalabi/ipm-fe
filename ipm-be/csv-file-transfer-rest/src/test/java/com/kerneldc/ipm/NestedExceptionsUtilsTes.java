package com.kerneldc.ipm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.NestedExceptionUtils;

import com.kerneldc.common.exception.ApplicationException;

class NestedExceptionsUtilsTes {

	@Test
	void testWhenExceptionHasCause() {
		var npe = new NullPointerException();
		var exception = new ApplicationException(npe);
		var realException = NestedExceptionUtils.getMostSpecificCause(exception);
		assertThat(realException, equalTo(npe));
	}
	
	@Test
	void testWhenExceptionDoesNotHaveCause() {
		var exception = new ApplicationException("Exception message");
		var realException = NestedExceptionUtils.getMostSpecificCause(exception);
		assertThat(realException, equalTo(exception));
	}

	@Test
	void testGettingMessage() {
		var exception = new ApplicationException("Exception message");
		var realException = NestedExceptionUtils.getMostSpecificCause(exception);
		assertThat(realException.getMessage(), equalTo(exception.getMessage()));
	}

	@Test
	void testGettingMessage2() {
		var npe = new NullPointerException("Variable xyz is null");
		var exception = new ApplicationException("Exception message", npe);
		var realException = NestedExceptionUtils.getMostSpecificCause(exception);
		assertThat(realException.getMessage(), equalTo(npe.getMessage()));
		exception.printStackTrace();
	}

}
