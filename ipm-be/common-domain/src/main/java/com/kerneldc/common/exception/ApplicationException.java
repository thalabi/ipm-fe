package com.kerneldc.common.exception;

public class ApplicationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ApplicationException() {
	}

	public ApplicationException(String arg0) {
		super(arg0);
	}

	public ApplicationException(Throwable arg0) {
		super(arg0);
	}

	public ApplicationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ApplicationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
