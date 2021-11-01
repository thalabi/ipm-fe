package com.kerneldc.ipm.rest.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

	private CustomUserDetails customUserDetails;
	private String token;
	
}
