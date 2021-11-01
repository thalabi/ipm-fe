package com.kerneldc.ipm.rest.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SimpleGrantedAuthorityMixIn {

	public SimpleGrantedAuthorityMixIn(@JsonProperty("authority") String role) {
	}

}
