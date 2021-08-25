package com.kerneldc.springsecurityjwt.security;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String resetPasswordJwt;

    @NotBlank
    private String newPassword;
    
    private String email;
    private String baseUrl;

}
