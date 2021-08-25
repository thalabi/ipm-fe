package com.kerneldc.springsecurityjwt.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.springsecurityjwt.exception.ApplicationException;
import com.kerneldc.springsecurityjwt.security.CustomUserDetails;
import com.kerneldc.springsecurityjwt.security.ForgotPasswordRequest;
import com.kerneldc.springsecurityjwt.security.JwtUtil;
import com.kerneldc.springsecurityjwt.security.LoginRequest;
import com.kerneldc.springsecurityjwt.security.LoginResponse;
import com.kerneldc.springsecurityjwt.security.ResetPasswordRequest;
import com.kerneldc.springsecurityjwt.security.service.SecurityService;

import freemarker.template.TemplateException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("securityController")
@RequiredArgsConstructor
@Slf4j
public class SecurityController {

    private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final SecurityService securityService;

    @GetMapping("/ping")
	public ResponseEntity<PingResponse> ping() {
    	LOGGER.info("Begin ...");
    	PingResponse pingResponse = new PingResponse();
    	pingResponse.setMessage("pong");
    	pingResponse.setTimestamp(LocalDateTime.now());
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(pingResponse);
    }

    @PostMapping("/authenticate")
	public ResponseEntity<LoginResponse> auhenticate(@Valid @RequestBody LoginRequest loginRequest) {
		LOGGER.debug("Begin ...");
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
		try {
			Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
			
			LOGGER.debug("authentication: {}", authentication);
			
			CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
			customUserDetails.setPassword("********");
			
			String token = jwtUtil.generateToken(customUserDetails);
			
			LoginResponse loginResponse = new LoginResponse(customUserDetails, token);
			
			LOGGER.debug("End ...");
			return ResponseEntity.ok(loginResponse);

		} catch (AuthenticationException ex) {
			LOGGER.error(ex.getMessage());
			LOGGER.debug("End ...");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
	}

    @PostMapping("/forgotPassword")
	public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
		LOGGER.debug("Begin ...");
		LOGGER.debug("forgotPasswordRequest: {}", forgotPasswordRequest);
		try {
			securityService.processForgotPasswordRequest(forgotPasswordRequest.getEmail(), forgotPasswordRequest.getBaseUrl());
		} catch (EntityNotFoundException e) {
			LOGGER.warn(e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (NoSuchAlgorithmException | MessagingException | IOException | TemplateException e) {
			LOGGER.error("Exception calling processForgotPasswordRequest with email: {} and baseUrl: {}",  forgotPasswordRequest.getEmail(), forgotPasswordRequest.getBaseUrl(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		LOGGER.debug("End ...");
		return ResponseEntity.ok(null);
    }

    @PostMapping("/resetPassword")
	public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
		LOGGER.debug("Begin ...");
		LOGGER.debug("resetPasswordRequest: {}", resetPasswordRequest);
		try {
			securityService.processResetPasswordRequest(resetPasswordRequest.getResetPasswordJwt(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getBaseUrl());
		} catch (ExpiredJwtException e) {
			LOGGER.warn(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This reset password link has expired. Please request another password reset.");
		} catch (SignatureException e) {
			LOGGER.warn(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This is not the latest reset password link. Please check your mailbox for the latest reset password email.");
		} catch (ApplicationException e) {
			LOGGER.warn(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Exception calling processResetPasswordRequest with resetPasswordJwt: {}, newPassword: ********, baseUrl: {}",  resetPasswordRequest.getResetPasswordJwt(), resetPasswordRequest.getBaseUrl(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		LOGGER.debug("End ...");
		return ResponseEntity.ok(null);
    }

}
