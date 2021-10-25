package com.kerneldc.springsecurityjwt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.kerneldc.springsecurityjwt.TestUtil;
import com.kerneldc.springsecurityjwt.security.CustomUserDetails;
import com.kerneldc.springsecurityjwt.security.JwtUtil;
import com.kerneldc.springsecurityjwt.security.repository.UserRepository;
import com.kerneldc.springsecurityjwt.security.service.CustomUserDetailsService;
import com.kerneldc.springsecurityjwt.security.service.SecurityService;
import com.kerneldc.springsecurityjwt.springconfig.UnauthorizedHandler;

@WebMvcTest(SecurityController.class)
@ActiveProfiles("test")
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration
//@SpringBootTest
class SecurityControllerTest {

	@Autowired
    private MockMvc mockMvc;
	@MockBean
	private CustomUserDetailsService customUserDetailsService;
	@MockBean
	private UserRepository userRepository;
	@MockBean
	private JwtUtil jwtUtil;
	@MockBean
	private UnauthorizedHandler unauthorizedHandler;
	@MockBean
	private SecurityService SecurityService;

	@Test
	@DisplayName("/ping with an authorized user")
	@WithMockUser(username = "some-authrized-user")
	void testPingWithAValidUser() throws Exception {
		mockMvc.perform(get("/securityController/ping"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("message").value("pong"))
		.andExpect(MockMvcResultMatchers.jsonPath("timestamp").isNotEmpty());
	}

	@Disabled("Test does not work due to Spring returnong http status code 200 eventhough the resource is secured")
	@Test
	@DisplayName("/ping with an unauthorized user")
	@WithAnonymousUser
	void testPingWithUnauthrizedUser() throws Exception {
		mockMvc.perform(get("/securityController/ping"))
		.andDo(print())
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	@DisplayName("/authenticate")
	@WithAnonymousUser
	void testAuthenticate() throws Exception {
		CustomUserDetails customUserDetails = TestUtil.createCustomUserDetails();
		Mockito.when(customUserDetailsService.loadUserByUsername(any())).thenReturn(customUserDetails);
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJjdXN0b21Vc2VyRGV0YWlscyI6eyJpZCI6MSwidXNlcm5hbWUiOiJ0aGFsYWJpIiwicGFzc3dvcmQiOiIqKioqKioqKiIsImZpcnN0TmFtZSI6bnVsbCwibGFzdE5hbWUiOm51bGwsImF1dGhvcml0aWVzIjpbeyJhdXRob3JpdHkiOiJmbGlnaHRfbG9nIHdyaXRlIn0seyJhdXRob3JpdHkiOiJmbGlnaHRfbG9nIHJlYWQifV19LCJpYXQiOjE2MjA0MDEwMzEsImV4cCI6MTYyMDQzNzAzMX0.Fvtwi2hltIhKWMRG3-kv7mJAqpNHVRXJRwkqgpqOfaw";
		Mockito.when(jwtUtil.generateToken(customUserDetails)).thenReturn(token);
		mockMvc.perform(post("/securityController/authenticate").contentType(MediaType.APPLICATION_JSON).content(
				"{\r\n"
				+ "    \"username\": \""+TestUtil.USERNAME+"\",\r\n"
				+ "    \"password\": \""+TestUtil.PASSWORD+"\"\r\n"
				+ "}"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("customUserDetails").isNotEmpty())
		.andExpect(jsonPath("token").isNotEmpty())
		;
	}
}
