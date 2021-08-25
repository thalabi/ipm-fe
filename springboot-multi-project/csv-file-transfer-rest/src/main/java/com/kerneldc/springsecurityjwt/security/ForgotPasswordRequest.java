package com.kerneldc.springsecurityjwt.security;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String baseUrl;

}
