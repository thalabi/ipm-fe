package com.kerneldc.ipm.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.HoldingPriceInterdayV;
import com.kerneldc.ipm.domain.InstrumentDueV;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

	@Value("${application.email.dailyMarketValueNotificationFrom}")
	private String dailyMarketValueNotificationFrom;
	@Value("${application.email.dailyMarketValueNotificationTo}")
	private String dailyMarketValueNotificationTo;

	@Value("${dailyMarketValueNotification.template.upArrowUrl}")
	private String upArrowUrl;
	@Value("${dailyMarketValueNotification.template.downArrowUrl}")
	private String downArrowUrl;

	@Value("${application.email.instrumentDueNotificationFrom}")
	private String instrumentDueNotificationFrom;
	@Value("${application.email.instrumentDueNotificationTo}")
	private String instrumentDueNotificationTo;

	
	private static final String DAILY_MARKET_VALUE_NOTIFICATION_SUBJECT = "Daily Market Value";
	private static final String DAILY_MARKET_VALUE_NOTIFICATION_TEMPLATE = "dailyMarketValueNotification.ftlh";
	private static final String DAILY_MARKET_VALUE_FAILURE_TEMPLATE = "dailyMarketValueFailure.ftlh";
	private static final String INSTRUMENT_DUE_NOTIFICATION_SUBJECT = "Instrument(s) Due";
	private static final String INSTRUMENT_DUE_NOTIFICATION_TEMPLATE = "instrumentDueNotification.ftlh";
	private JavaMailSender javaMailSender;
	private Configuration freeMarkerConfiguration;
	
	public EmailService(JavaMailSender emailSender, Configuration freeMarkerConfiguration) {
		this.javaMailSender = emailSender;
		this.freeMarkerConfiguration = freeMarkerConfiguration;
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
	
	public void sendDailyMarketValueNotification(LocalDateTime todaysSnapshot, BigDecimal todaysMarketValue, Float percentChange, List<HoldingPriceInterdayV> nMarketValues, ApplicationException priceHoldingsExceptions) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(dailyMarketValueNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(dailyMarketValueNotificationTo));
			mimeMessageHelper.setSubject(DAILY_MARKET_VALUE_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processDailyMarketValueNotificationTemplate(todaysSnapshot, todaysMarketValue, percentChange, nMarketValues, priceHoldingsExceptions), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending Daily Market Value Notification email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Sent daily market value notification email to: {}", dailyMarketValueNotificationTo);
	}
	public void sendDailyMarketValueFailure(ApplicationException priceHoldingsExceptions) throws ApplicationException {
		sendFailureEmail(priceHoldingsExceptions, dailyMarketValueNotificationFrom, dailyMarketValueNotificationTo, DAILY_MARKET_VALUE_NOTIFICATION_SUBJECT);
		LOGGER.info("Sent daily market value notification failure email to: {}", dailyMarketValueNotificationTo);
	}
	public void sendInstrumentDueNotification(Long daysToNotify, List<InstrumentDueV> instrumentDueVList) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(instrumentDueNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(instrumentDueNotificationTo));
			mimeMessageHelper.setSubject(INSTRUMENT_DUE_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processInstrumentDueNotificationTemplate(instrumentDueVList, daysToNotify), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending instrument(s) due notification email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Sent instrument(s) due notification email to: {}", instrumentDueNotificationTo);
	}

	public void sendInstrumentDueFailure(ApplicationException instrumentDueExceptions) throws ApplicationException {
		sendFailureEmail(instrumentDueExceptions, instrumentDueNotificationFrom, instrumentDueNotificationTo, INSTRUMENT_DUE_NOTIFICATION_SUBJECT);
		LOGGER.info("Sent instrument due failure notification email to: {}", instrumentDueNotificationTo);
	}
	private void sendFailureEmail(ApplicationException applicationExceptions, String emailFromAddress, String emailToAddress, String emailSubject) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailFromAddress);
			mimeMessageHelper.setTo(InternetAddress.parse(emailToAddress));
			mimeMessageHelper.setSubject(emailSubject);
			mimeMessageHelper.setText(processDailyMarketValueFailureTemplate(applicationExceptions), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending failure email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Sent daily market value notification email to: {}", dailyMarketValueNotificationTo);
	}

	private String processDailyMarketValueNotificationTemplate(LocalDateTime todaysSnapshot, BigDecimal todaysMarketValue, Float percentChange, List<HoldingPriceInterdayV> nMarketValues, ApplicationException priceHoldingsExceptions) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("todaysSnapshot", TimeUtils.toDate(todaysSnapshot));
		templateModelMap.put("todaysMarketValue", todaysMarketValue);
		templateModelMap.put("percentChange", percentChange);
		templateModelMap.put("nMarketValues", nMarketValues);
		templateModelMap.put("priceHoldingsExceptions", priceHoldingsExceptions);
		templateModelMap.put("upArrowUrl", upArrowUrl);
		templateModelMap.put("downArrowUrl", downArrowUrl);

		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(DAILY_MARKET_VALUE_NOTIFICATION_TEMPLATE), templateModelMap);
	}
	// TO DO continue
	private String processInstrumentDueNotificationTemplate(List<InstrumentDueV> instrumentDueVList, Long daysToNotify) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("instrumentDueVList", instrumentDueVList);
		templateModelMap.put("daysToNotify", daysToNotify);

		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(INSTRUMENT_DUE_NOTIFICATION_TEMPLATE), templateModelMap);
	}
	private String processDailyMarketValueFailureTemplate(ApplicationException priceHoldingsExceptions) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("priceHoldingsExceptions", priceHoldingsExceptions);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(DAILY_MARKET_VALUE_FAILURE_TEMPLATE), templateModelMap);
	}
}
