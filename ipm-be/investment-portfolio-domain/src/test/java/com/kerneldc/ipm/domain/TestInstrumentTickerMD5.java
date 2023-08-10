package com.kerneldc.ipm.domain;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

class TestInstrumentTickerMD5 {

	@Test
	void nameMd5Test() {
		var name = """
				Something went wrong. Session ended (oAuthEvent: token_revoke_error)
Welcome to IPM application
Login to see the protected pages
				""";
		var md5 = DigestUtils.md5Digest(name.getBytes());
		
		System.out.println(md5 + ", is " + md5.length + " bytes long");
		assertThat(md5.length, equalTo(16));
		String ticker = md5.toString();
		System.out.println(ticker);
	}

}
