package com.kerneldc.ipm.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.kerneldc.ipm.rest.security.domain.user.User;
import com.kerneldc.ipm.rest.security.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class HmacSHA256KeyConverterTest {

	@Autowired
	private UserRepository userRepository;
	
	@Test
	void testWhenPasswordResetTokenKeyIsNull() {
		User user = new User();
		user.setUsername("username1");
		user.setPassword("password");
		user.setEnabled(true);
		user.setEmail("thalabi1@acme.com");
		user.setCellPhone("8005551212");
		userRepository.save(user);
		assertThat(user.getId()).isGreaterThan(0l);
		assertThat(user.getResetPasswordJwtKey()).isNull();
	}
	@Test
	void testWhenPasswordResetTokenKeyIsNotNull() throws NoSuchAlgorithmException {
		User user = new User();
		user.setUsername("username2");
		user.setPassword("password");
		user.setEnabled(true);
		user.setEmail("thalabi2@acme.com");
		user.setCellPhone("8005551212");
		user.setResetPasswordJwtKey(KeyGenerator.getInstance("HmacSHA256").generateKey());
		userRepository.save(user);
		assertThat(user.getId()).isGreaterThan(0l);
		assertThat(user.getResetPasswordJwtKey()).isNotNull();
		LOGGER.info("user.getPasswordResetTokenKey(): {}", user.getResetPasswordJwtKey());
	}
}
