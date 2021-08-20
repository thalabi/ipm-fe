package com.kerneldc.springsecurityjwt.security;

public class SecurityConstants {

	private SecurityConstants() {
		throw new IllegalStateException("Utility class");
	}
	public static final String AUTH_HEADER_NAME = "Authorization";
	public static final String AUTH_HEADER_SCHEMA = "Bearer";
	
}
