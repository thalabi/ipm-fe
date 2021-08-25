package com.kerneldc.springsecurityjwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

	static final String RESET_PASSWORD_EMAIL_FROM = "noreply-springsecurityjwt@kerneldc.com";
	private static final String RESET_PASSWORD_EMAIL_SUBJECT = "Reset password";
	private static final String RESET_PASSWORD_CONFIRMATION_EMAIL_SUBJECT = "Reset password confirmation";
	private static final String RESET_PASSWORD_EMAIL_TEMPLATE = "resetPasswordEmail.ftlh";
	private static final String RESET_PASSWORD_CONFIRMATION_EMAIL_TEMPLATE = "resetPasswordConfirmationEmail.ftlh";
	private JavaMailSender javaMailSender;
	private Configuration freeMarkerConfiguration;
	private int resetPasswordJwtExpiryInMinutes;
	
	public EmailService(JavaMailSender emailSender, Configuration freeMarkerConfiguration, @Value("${application.security.jwt.token.resetPasswordJwtExpiryInMinutes:60}" /* default of 1 hour */) int resetPasswordJwtExpiryInMinutes) {
		this.javaMailSender = emailSender;
		this.freeMarkerConfiguration = freeMarkerConfiguration;
		this.resetPasswordJwtExpiryInMinutes = resetPasswordJwtExpiryInMinutes;
	}

	public void sendPasswordResetEmail(String to, String resetPasswordUrl) throws MessagingException, IOException, TemplateException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		mimeMessageHelper.setFrom(RESET_PASSWORD_EMAIL_FROM);
		mimeMessageHelper.setTo(to);
		mimeMessageHelper.setSubject(RESET_PASSWORD_EMAIL_SUBJECT);
		mimeMessageHelper.setText(processResetPasswordTemplate(resetPasswordJwtExpiryInMinutes/60, resetPasswordUrl), true);
		javaMailSender.send(mimeMessage);
		LOGGER.info("Sent password reset email to: {}", to);
	}
	
	public void sendPasswordResetConfirmationEmail(String to, String loginUrl) throws MessagingException, IOException, TemplateException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		mimeMessageHelper.setFrom(RESET_PASSWORD_EMAIL_FROM);
		mimeMessageHelper.setTo(to);
		mimeMessageHelper.setSubject(RESET_PASSWORD_CONFIRMATION_EMAIL_SUBJECT);
		mimeMessageHelper.setText(processResetPasswordConfirmationTemplate(loginUrl), true);
		javaMailSender.send(mimeMessage);
		LOGGER.info("Sent password reset confirmation email to: {}", to);
	}

	public void sendSmsEmail(String from, String to, String smsMessage) throws MessagingException, IOException, TemplateException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		mimeMessageHelper.setFrom(from);
		mimeMessageHelper.setTo(InternetAddress.parse(to));
		mimeMessageHelper.setText(smsMessage);
		javaMailSender.send(mimeMessage);
		LOGGER.info("Sent sms email to: {}", to);
	}

	private String processResetPasswordTemplate(int linkExpiryInHours, String resetPasswordUrl) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("linkExpiryInHours", linkExpiryInHours);
		templateModelMap.put("resetPasswordUrl", resetPasswordUrl);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(RESET_PASSWORD_EMAIL_TEMPLATE), templateModelMap);
	}
	private String processResetPasswordConfirmationTemplate(String loginUrl) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("loginUrl", loginUrl);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(RESET_PASSWORD_CONFIRMATION_EMAIL_TEMPLATE), templateModelMap);
	}
}
