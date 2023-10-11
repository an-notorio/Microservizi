package com.example.loginmicroservizi.controller;

import com.example.loginmicroservizi.dto.AuthenticationRequest;
import com.example.loginmicroservizi.dto.AuthenticationResponse;
import com.example.loginmicroservizi.dto.RegisterRequest;
import com.example.loginmicroservizi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {

    private final AuthenticationService service;

    @Operation(summary = "register a USER")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(service.register(request));
    }

    @Operation(summary = "login")
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Operation(summary = "update a USER")
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> update(@RequestBody RegisterRequest request, @PathVariable Integer userId) {
        service.updateUser(request, userId);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @Operation(summary = "delete a USER")
    @PutMapping("/delete/{userId}")
    public ResponseEntity<?> delete(@PathVariable Integer userId) {
        service.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }




}

