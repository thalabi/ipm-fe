package com.kerneldc.ipm.rest.springconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories({"com.kerneldc.common.repository","com.kerneldc.ipm.rest.security.repository","com.kerneldc.ipm.rest.csv.repository","com.kerneldc.ipm.repository"})
public class JpaConfiguration {

}
