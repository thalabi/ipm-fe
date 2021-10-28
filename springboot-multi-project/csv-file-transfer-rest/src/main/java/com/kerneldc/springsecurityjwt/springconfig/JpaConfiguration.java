package com.kerneldc.springsecurityjwt.springconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories({"com.kerneldc.springsecurityjwt.security.repository","com.kerneldc.springsecurityjwt.csv.repository","com.kerneldc.portfolio.repository"})
public class JpaConfiguration {

}
