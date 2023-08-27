package com.kerneldc.ipm.rest.springconfig;


import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${application.security.corsFilter.corsMaxAgeInSecs:3600}")
    private long corsMaxAgeInSecs;
    
    @Autowired
    private EntityManager entityManager;

    // configure application
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
    	configCorsRegistry(corsRegistry);
    }
    
    // configure data rest
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration repositoryRestConfiguration, CorsRegistry corsRegistry) {
    	configRepositoryRest(repositoryRestConfiguration);
    	configCorsRegistry(corsRegistry);
    }
    
    private void configRepositoryRest(RepositoryRestConfiguration repositoryRestConfiguration) {
    	// expose entity id for JPA data rest
    	repositoryRestConfiguration.exposeIdsFor(entityManager.getMetamodel().getEntities()
                .stream().map(Type::getJavaType).toArray(Class[]::new));
	}

	private void configCorsRegistry(CorsRegistry corsRegistry) {
		corsRegistry.addMapping("/**").allowedOrigins(corsUrlsToAllow).maxAge(corsMaxAgeInSecs)
		.allowedMethods("GET", "HEAD", "POST", "DELETE", "PUT") // by default GET, HEAD, and POST are allowed
		//.allowedHeaders("Content-Disposition").exposedHeaders("Content-Disposition")
		.allowedHeaders("*").exposedHeaders("*")
		;
    }
}
