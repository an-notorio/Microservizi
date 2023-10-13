package com.example.loginmicroservizi.controller;

import com.example.loginmicroservizi.dto.AuthenticationRequest;
import com.example.loginmicroservizi.dto.AuthenticationResponse;
import com.example.loginmicroservizi.dto.RegisterRequest;
import com.example.loginmicroservizi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {

    private final AuthenticationService service;

    @Operation(summary = "register a USER")
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ){
        return service.register(request);
    }

    @Operation(summary = "login")
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ){
            return service.authenticate(request);
    }

    @Operation(summary = "update a USER")
    @PutMapping("/update/{userId}")
    @PreAuthorize("@securityService.hasPermission(#request, #userId)")
    public ResponseEntity<?> update(@RequestBody RegisterRequest registerRequest, @PathVariable Integer userId, HttpServletRequest request) {
        service.updateUser(registerRequest, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "delete a USER")
    @Secured("USER")
    @PutMapping("/delete/{userId}")
    public ResponseEntity<?> delete(@PathVariable Integer userId) {
        service.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}