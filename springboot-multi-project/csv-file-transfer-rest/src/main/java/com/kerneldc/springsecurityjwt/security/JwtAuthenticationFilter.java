package com.kerneldc.springsecurityjwt.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		LOGGER.debug("Begin ...");
        LOGGER.debug("authenticated: {}", SecurityContextHolder.getContext().getAuthentication() == null ? null : SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		LOGGER.debug("url: {}", request.getRequestURL() + "?" + request.getQueryString());
		String token = getTokenFromRequest(request);
		LOGGER.debug("token: {}", token);
		if (StringUtils.isNotEmpty(token) && jwtUtil.validateToken(token)) {
			
			CustomUserDetails customUserDetails = jwtUtil.getCustomUserDetailsFromJwt(token);
			LOGGER.debug("customUserDetails: {}", customUserDetails);
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
					new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
			usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            
            LOGGER.debug("authenticated: {}", SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
		} 
		LOGGER.debug("End ...");
        filterChain.doFilter(request, response);
	}

	private String getTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(SecurityConstants.AUTH_HEADER_NAME);
        LOGGER.debug("authorizationHeader: {}", authorizationHeader);
        String prefix = SecurityConstants.AUTH_HEADER_SCHEMA+StringUtils.SPACE;
        if (StringUtils.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith(prefix)) {
            return authorizationHeader.substring(prefix.length());
        } else {
        	return null;
        }
    }
}
