package com.kerneldc.springsecurityjwt.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer, RepositoryRestConfigurer {

    @Value("${application.security.corsFilter.corsUrlsToAllow}")
    private String[] corsUrlsToAllow;

    @Value("${application.security.corsFilter.corsMaxAgeInSecs}")
    private long corsMaxAgeInSecs;

    // configure application
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
    	configCorsRegistry(corsRegistry);
    }
    
    // configure data rest
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry corsRegistry) {
    	configCorsRegistry(corsRegistry);
    }
    
    private void configCorsRegistry(CorsRegistry corsRegistry) {
		corsRegistry.addMapping("/**").allowedOrigins(corsUrlsToAllow).maxAge(corsMaxAgeInSecs)
		.allowedMethods("GET", "HEAD", "POST", "DELETE") // by default GET, HEAD, and POST are allowed
		//.allowedHeaders("Content-Disposition").exposedHeaders("Content-Disposition")
		.allowedHeaders("*").exposedHeaders("*")
		;
    }
}
