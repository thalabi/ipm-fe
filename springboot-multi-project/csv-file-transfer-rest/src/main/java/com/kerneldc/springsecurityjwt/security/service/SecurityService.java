package com.kerneldc.springsecurityjwt.security.service;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.kerneldc.springsecurityjwt.EmailService;
import com.kerneldc.springsecurityjwt.exception.ApplicationException;
import com.kerneldc.springsecurityjwt.security.JwtUtil;
import com.kerneldc.springsecurityjwt.security.JwtUtil.JwtAndKey;
import com.kerneldc.springsecurityjwt.security.domain.user.User;
import com.kerneldc.springsecurityjwt.security.repository.UserRepository;

import freemarker.template.TemplateException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

	private final EmailService emailService;
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	public void processForgotPasswordRequest(String email, String clientAppBaseUrl) throws MessagingException, IOException, TemplateException, NoSuchAlgorithmException {
		
		List<User> userList = userRepository.findByEmail(email);
		if (CollectionUtils.isEmpty(userList)) {
			throw new EntityNotFoundException("Could not find a user with this email.");
		}
		final User user = userList.get(0);
		final JwtAndKey jwtAndKey =  jwtUtil.generateResetPasswordJwt(user.getUsername());
		
		//user.setEnabled(false);
		user.setResetPasswordJwtKey(jwtAndKey.key());
		user.setModified(LocalDateTime.now());
		userRepository.save(user);

		LOGGER.debug("baseUrl: {}", clientAppBaseUrl);
		final String resetPasswordUrl = clientAppBaseUrl + "/resetPassword?token=" + jwtAndKey.jwt();
		LOGGER.debug("resetPasswordUrl: {}", resetPasswordUrl);

		emailService.sendPasswordResetEmail(email, resetPasswordUrl);
	}
	
	public void processResetPasswordRequest(String resetPasswordJwt, String newPassword, String clientAppBaseUrl) throws MessagingException, IOException, TemplateException, ApplicationException {
		// throws ExpiredJwtException
		final String username = jwtUtil.getUsernameFromResetPasswordJwt(resetPasswordJwt);
		LOGGER.debug("username: {}", username);
		final List<User> userList = userRepository.findByUsername(username);
		if (CollectionUtils.isEmpty(userList)) {
			throw new EntityNotFoundException(String.format("Could not find a user with username: %s", username));
		}
		final User user = userList.get(0);
		final Key resetPasswordJwtKey = user.getResetPasswordJwtKey();
		if (resetPasswordJwtKey == null) {
			throw new ApplicationException("Password has already been reset. Please request another password reset.");
		}
		// parse resetPasswordJwtKey and make sure it is signed with the key supplied
		Jwts.parserBuilder().setSigningKey(resetPasswordJwtKey).build().parseClaimsJws(resetPasswordJwt);
		LOGGER.debug("resetPasswordJwt is {} valid");
		//user.setEnabled(true);
		user.setPassword(newPassword);
		user.setResetPasswordJwtKey(null);
		user.setModified(LocalDateTime.now());
		userRepository.save(user);
		LOGGER.info("Username: {}, updated", username);
		final String loginUrl = clientAppBaseUrl + "/login";
		LOGGER.debug("loginUrl: {}", loginUrl);
		emailService.sendPasswordResetConfirmationEmail(user.getEmail(), loginUrl);	
	}
}
