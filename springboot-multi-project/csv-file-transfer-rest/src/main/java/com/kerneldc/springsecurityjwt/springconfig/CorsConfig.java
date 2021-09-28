package com.kerneldc.springsecurityjwt.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${application.security.corsFilter.corsUrlsToAllow}")
    private String[] corsUrlsToAllow;

    @Value("${application.security.corsFilter.corsMaxAgeInSecs}")
    private long corsMaxAgeInSecs;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins(corsUrlsToAllow).maxAge(corsMaxAgeInSecs)
		.allowedMethods("GET", "HEAD", "POST", "DELETE") // by default GET, HEAD, and POST are allowed
		//.allowedHeaders("Content-Disposition").exposedHeaders("Content-Disposition")
		.allowedHeaders("*").exposedHeaders("*")
		;
		
    }
}
