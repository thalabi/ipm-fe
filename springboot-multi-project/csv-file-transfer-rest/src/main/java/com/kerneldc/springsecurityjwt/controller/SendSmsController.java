package com.kerneldc.springsecurityjwt.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.kerneldc.springsecurityjwt.SmsService;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("sendSmsController")
@RequiredArgsConstructor
@Slf4j
public class SendSmsController {

	private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private final SmsService smsService;

    @GetMapping("/sendSms")
	public ResponseEntity<PingResponse> sendSms(@RequestParam String cellPhone) {
    	LOGGER.info("Begin ...");
    	var pingResponse = new PingResponse();
    	cellPhone = validateCellPhone(cellPhone);
    	if (StringUtils.isEmpty(cellPhone)) {
    		pingResponse.setMessage("Invalid cell phone number");
    	} else {
			try {
				smsService.sendSecurityCode(cellPhone);
		    	pingResponse.setMessage("Message sent successfully");
			} catch (MessagingException | IOException | TemplateException e) {
				var errorMessage = String.format("Error emailing sms message: %s", e.getMessage() + (e.getCause() != null ? ", "+e.getCause().getMessage() : StringUtils.EMPTY));
				LOGGER.error(errorMessage);
				e.printStackTrace();
		    	pingResponse.setMessage(errorMessage);
			}
    	}
    	pingResponse.setTimestamp(LocalDateTime.now());
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(pingResponse);
    }
    

    private String validateCellPhone(String cellPhone) {
		PhoneNumber number;
		try {
			number = phoneNumberUtil.parse(cellPhone, "CA");
			return phoneNumberUtil.isValidNumber(number) ? phoneNumberUtil.getNationalSignificantNumber(number) : StringUtils.EMPTY;
		} catch (NumberParseException e) {
			return StringUtils.EMPTY;
		}
    }
}
