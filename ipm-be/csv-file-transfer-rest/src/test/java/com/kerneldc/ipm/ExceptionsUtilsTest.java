package com.kerneldc.ipm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import com.kerneldc.common.exception.ApplicationException;

class ExceptionsUtilsTest {

	@Test
	void testWhenExceptionHasCause() {
		var npe = new NullPointerException();
		var exception = new ApplicationException(npe);
		var realException = ExceptionUtils.getRootCause(exception);
		assertThat(realException, equalTo(npe));
	}
	
	@Test
	void testWhenExceptionDoesNotHaveCause() {
		var exception = new ApplicationException("Exception message");
		var realException = ExceptionUtils.getRootCause(exception);
		assertThat(realException, equalTo(exception));
	}

	@Test
	void testGettingMessage() {
		var exception = new ApplicationException("Exception message");
		var realExceptionMessage = ExceptionUtils.getRootCauseMessage(exception);
		assertThat(realExceptionMessage, equalTo("ApplicationException: "+exception.getMessage()));
	}

	@Test
	void testGettingMessage2() {
		var npe = new NullPointerException("Variable xyz is null");
		var exception = new ApplicationException("Exception message", npe);
		var realExceptionMessage = ExceptionUtils.getRootCauseMessage(exception);
		assertThat(realExceptionMessage, equalTo("NullPointerException: "+npe.getMessage()));
	}

}
