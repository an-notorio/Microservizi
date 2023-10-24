package com.example.loginmicroservizi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordDto {
    private String password;
    private String repeatPassword;
}
