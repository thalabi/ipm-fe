package com.kerneldc.ipm.rest.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class SecurityControllerIntegrationTest {

	@Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

	@BeforeEach
	public void setup() throws Exception {
		mockMvc = webAppContextSetup(webApplicationContext)
				.apply(springSecurity()) // enable Spring Security for these tests
				.build();
	}

	@Test
	@DisplayName("/ping with an authorized user")
	@WithMockUser(username = "some-authrized-user")
	void testPingWithAValidUser() throws Exception {
		mockMvc.perform(get("/securityController/ping"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("message").value("pong"))
		.andExpect(jsonPath("timestamp").isNotEmpty());
	}

	@Test
	@DisplayName("/ping with an unauthorized user")
	void testPingWithUnauthrizedUser() throws Exception {
		mockMvc.perform(get("/securityController/ping"))
		.andDo(print())
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	@DisplayName("/authenticate")
	void testAuthenticate() throws Exception {
		mockMvc.perform(post("/securityController/authenticate").contentType(MediaType.APPLICATION_JSON).content(
				"{\r\n"
				+ "    \"username\": \"thalabi\",\r\n"
				+ "    \"password\": \"123456\"\r\n"
				+ "}"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("customUserDetails").isNotEmpty())
		.andExpect(jsonPath("token").isNotEmpty())
		;
	}
}
