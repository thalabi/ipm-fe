package com.kerneldc.ipm.rest.springconfig;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FreeMarkerConfig {

	@Bean
    public freemarker.template.Configuration getFreeMarkerConfiguration() throws IOException, TemplateException {
        FreeMarkerConfigurationFactoryBean fmConfigFactoryBean = new FreeMarkerConfigurationFactoryBean();
        var freeMarkerConfig = fmConfigFactoryBean.createConfiguration();
        LOGGER.info("FreeMarker: Setting locale to {}", Locale.getDefault());
        freeMarkerConfig.setLocale(Locale.getDefault());
        return freeMarkerConfig;
    }}
