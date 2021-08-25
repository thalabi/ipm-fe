package com.kerneldc.springsecurityjwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.kerneldc.common.enums.CountryEnum;
import com.kerneldc.springsecurityjwt.csv.repository.AreaCodeRepository;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
	
	private static final String SMS_SECURITY_CODE_FROM = EmailService.RESET_PASSWORD_EMAIL_FROM;
	private static final String SMS_SECURITY_CODE_TEMPLATE = "smsSecurityCode.ftlh";
	private static final String CANADIAN_CARRIERS_EMAILS_TEMPLATE = "canadianCarriersEmails.ftlh";
	private static final String AMERICAN_CARRIERS_EMAILS_TEMPLATE = "americanCarriersEmails.ftlh";
	private static final int SECURITY_CODE_LOWER_BOUND = 100000;
	private static final int SECURITY_CODE_UPPER_BOUND = 999999;
	private final Configuration freeMarkerConfiguration;
	private final EmailService emailService;
	private final AreaCodeRepository areaCodeRepository; 

	public void sendSecurityCode(String cellPhone) throws MessagingException, IOException, TemplateException {
		var smsSecurityCodeMessage = getSmsSecurityCodeMessage();
		LOGGER.debug("smsSecurityCodeMessage: {}", smsSecurityCodeMessage);
		
		emailService.sendSmsEmail(SMS_SECURITY_CODE_FROM, processCanadianCarriersEmailsTemplate(cellPhone), smsSecurityCodeMessage);
	}
	
	private String getSmsSecurityCodeMessage() throws IOException, TemplateException {
		int securityCode = getRandomSecurityCode();
		LOGGER.debug("securityCode: {}", securityCode);
		return processSmsSecurityCodeTemplate(securityCode);
	}
	private String processSmsSecurityCodeTemplate(int securityCode) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("securityCode", String.valueOf(securityCode));
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(SMS_SECURITY_CODE_TEMPLATE), templateModelMap);
	}

	private String processCanadianCarriersEmailsTemplate(String cellPhone) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("cellPhone", cellPhone);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(getCarriersEmailsTemplate(cellPhone)), templateModelMap);
	}
	
	private int getRandomSecurityCode() {
		var random = new Random(Thread.currentThread().getId());
		return random.nextInt(SECURITY_CODE_UPPER_BOUND - SECURITY_CODE_LOWER_BOUND) + SECURITY_CODE_LOWER_BOUND;
	}
	
	private String getCarriersEmailsTemplate(String cellPhone) {
		var areaCodeList = areaCodeRepository.findByCode(cellPhone.substring(0,3));
		if (CollectionUtils.isEmpty(areaCodeList)) {
			throw new EntityNotFoundException(String.format("Could not find area code [%s] in area_code table", cellPhone.substring(0,3)));
		}
		LOGGER.debug("areaCode: {}", areaCodeList.get(0));
		if (areaCodeList.get(0).getCountry() == CountryEnum.CAN) {
			return CANADIAN_CARRIERS_EMAILS_TEMPLATE;
		} else {
			return AMERICAN_CARRIERS_EMAILS_TEMPLATE;
		}
	}
}
