package com.example.loginmicroservizi.dto;

import com.example.loginmicroservizi.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotNull(message = "cannot be blank")
    private String firstName;
    @NotNull(message = "cannot be blank")
    private String lastName;
    @Email(message = "invali email address")
    private String email;
    @NotNull(message = "cannot be blank")
    private String password;
    private List<Role> role;
}
