package com.kerneldc.ipm.rest.springconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import com.kerneldc.ipm.rest.security.service.CustomUserDetailsService;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfig /*extends WebSecurityConfigurerAdapter*/ {
	
	@Value("${application.security.disableSecurity:false}")
	private boolean disableSecurity;

    
	@Autowired
	private KeycloakJwtRolesConverter keycloakJwtRolesConverter;
	
	@Autowired
	private CustomUserDetailsService customUserDetailsService;
//	@Autowired
//	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private UnauthorizedHandler unauthorizedHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
	
	/**
	 * Configure AuthenticationManager to use our CustomUserDetailsService and PasswordEncoder
	 */
//	@Override
//    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
//    }

//	@Override
//    protected void configure(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity
//			.cors()
//				.and()
//	    	.csrf().disable()
//	    	.exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
//	    		.and()
//	        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//		if (disableSecurity) {
//			LOGGER.warn("*** appliction security is currently disabled ***");
//			LOGGER.warn("*** to enable set application.security.disableSecurity to false ***");
//			httpSecurity.authorizeRequests().anyRequest().permitAll();
//			return;
//		}
//		
//		httpSecurity.authorizeRequests()
//				.mvcMatchers("/appInfoController/*", "/securityController/authenticate",
//						"/securityController/forgotPassword", "/securityController/resetPassword",
//						/*"/data-rest/*",*/ "/actuator/*").permitAll()
//				.anyRequest().authenticated();
//
//		// Add our jwtAuthenticationFilter
//		httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		DelegatingJwtGrantedAuthoritiesConverter authoritiesConverter =
				// Using the delegating converter multiple converters can be combined
				new DelegatingJwtGrantedAuthoritiesConverter(
						// First add the default converter
						new JwtGrantedAuthoritiesConverter(),
						// Second add our custom Keycloak specific converter
						keycloakJwtRolesConverter);

		// Set up http security to use the JWT converter defined above
		httpSecurity.oauth2ResourceServer().jwt()
				.jwtAuthenticationConverter(jwt -> new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt), keycloakJwtRolesConverter.getUsername(jwt)));

		//return httpSecurity.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll())
				// .authorizeRequests(authorizeRequests ->
				// authorizeRequests.anyRequest().authenticated())
				// .authorizeRequests(authorizeRequests ->
				// authorizeRequests.anyRequest().hasRole("kerneldc-realm-user-role"))
				// .authorizeRequests(authorizeRequests ->
				// authorizeRequests.anyRequest().hasRole("SCOPE_PROFILE"))

//		httpSecurity.authorizeRequests()
//		.mvcMatchers("/sandboxController/noBearerTokenPing", "/actuator/*").permitAll()
//		.mvcMatchers("/protected/sandboxController/getUserInfo").hasRole("realm_sso-app-user-role")
//		//.mvcMatchers("/noBearerTokenPing").hasRole("realm_sso2-app-admin-role")
//		;
		if (disableSecurity) {
			LOGGER.warn("*** appliction security is currently disabled ***");
			LOGGER.warn("*** to enable set application.security.disableSecurity to false ***");
			httpSecurity.authorizeRequests().anyRequest().permitAll();
		} else {
			httpSecurity.authorizeRequests()
			.mvcMatchers("/appInfoController/*", "/actuator/*").permitAll()
			.anyRequest().authenticated();
		}
		
		httpSecurity.exceptionHandling(
						exception -> exception.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
								.accessDeniedHandler(new BearerTokenAccessDeniedHandler()))

				.cors().and().csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		return httpSecurity.build();
	}

}
