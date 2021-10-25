package com.kerneldc.springsecurityjwt;

import java.util.Arrays;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kerneldc.springsecurityjwt.security.CustomUserDetails;

public class TestUtil {

	public static final String PASSWORD = "password";
	public static final String USERNAME = "jdoe";
	public static CustomUserDetails createCustomUserDetails() {
		CustomUserDetails customUserDetails = new CustomUserDetails();
		customUserDetails.setId(null);
		customUserDetails.setUsername(USERNAME);
		customUserDetails.setPassword(new BCryptPasswordEncoder().encode(PASSWORD));
		customUserDetails.setFirstName("John");
		customUserDetails.setLastName("Doe");

		SimpleGrantedAuthority a1 = new SimpleGrantedAuthority("permission1");
		SimpleGrantedAuthority a2 = new SimpleGrantedAuthority("permission2");
		SimpleGrantedAuthority a3 = new SimpleGrantedAuthority("permission3");
		customUserDetails.setAuthorities(Arrays.asList(a1, a2, a3));
		return customUserDetails;
	}

}
