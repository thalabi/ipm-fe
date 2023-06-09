package com.kerneldc.ipm.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;

//@SpringBootTest
//@ActiveProfiles("test")

@Slf4j
class FeeeMarkerTest {

	//@Autowired
    //private FreeMarkerConfigurationFactoryBean freeMarkerConfigurationFactoryBean;
	@Autowired
	private Configuration freeMarkerConfiguration;
	
	@Disabled("until can figure out how to pass jsypt encryptor password")
	@Test
	void testHelloTemplate() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
		
		//LOGGER.debug("freeMarkerConfigurationFactoryBean: {}", freeMarkerConfigurationFactoryBean);
		LOGGER.debug("freeMarkerConfiguration: {}", freeMarkerConfiguration);
		
		Map<String, Object> freeMarkerModelMap = new HashMap<>();
		freeMarkerModelMap.put("user", "Big Joe");
		freeMarkerModelMap.put("url", "http://localhost:4200/resetPassword?token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGFsYWJpIiwiaWF0IjoxNjIxNTQxODE3LCJleHAiOjE2MjE1NDU0MTd9.BN5kwIE9eThyZHM73Oc2ISqG6R-PlDyNRuiT1469QcQ");
		
		
		//String result = FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfigurationFactoryBean.getObject().getTemplate("hello.ftlh"), freeMarkerModelMap);
		String result2 = FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate("hello.ftlh"), freeMarkerModelMap);
		LOGGER.debug("result: {}", result2);
		
		assertThat(result2).contains("Big Joe");
	}
}
