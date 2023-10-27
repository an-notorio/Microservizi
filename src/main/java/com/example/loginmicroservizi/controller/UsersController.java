package com.example.loginmicroservizi.controller;

import com.example.loginmicroservizi.dto.*;
import com.example.loginmicroservizi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {

    private final AuthenticationService service;

    @Operation(summary = "register a USER")
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest request
    ){
        return service.register(request);
    }

    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token")String confirmationToken) {
        return service.confirmEmail(confirmationToken);
    }

    @Operation(summary = "login")
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ){
            return service.authenticate(request);
    }

    @Operation(summary = "refresh token")
    @PostMapping("/refresh-token")
    public void refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    @Operation(summary = "update a USER")
    @PutMapping("/update/{userId}")
    @PreAuthorize("@securityService.hasPermission(#request, #userId)")
    public ResponseEntity<?> update(@RequestBody RegisterRequest registerRequest, @PathVariable Integer userId, HttpServletRequest request) {
        service.updateUser(registerRequest, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "delete a USER")
//    @Secured("USER")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> delete(@PathVariable Integer userId) {
        service.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/prova")
    public ResponseEntity<?> getProva() {
        String x = "ciao prova riuscita";
        return new ResponseEntity<> (x,HttpStatus.OK);
    }
    @Secured("ADMIN")
    @GetMapping("/getUsersTrue")
    public ResponseEntity<?> getUsersTrue() {
        return new ResponseEntity<> (service.findAll(true),HttpStatus.OK);
    }

    @Secured("ADMIN")
    @GetMapping("/getUsersFalse")
    public ResponseEntity<?> getUsersFalse() {
        return new ResponseEntity<> (service.findAll(false),HttpStatus.OK);
    }
    @GetMapping("/mail")
    public ResponseEntity<?> sendMail() throws MessagingException {
        service.triggerMail("AAA","hello from mail!");
        return new ResponseEntity<> (HttpStatus.OK);
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto email) throws MessagingException {
        service.forgotPassword(email.getMail());
        return new ResponseEntity<> (HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    public  ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto, @RequestParam(required = false) String token){
        return service.resetPassword(resetPasswordDto, token);
    }


}