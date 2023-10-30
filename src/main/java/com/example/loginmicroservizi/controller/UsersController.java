package com.example.loginmicroservizi.controller;

import com.example.loginmicroservizi.dto.*;
import com.example.loginmicroservizi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
//@SecurityRequirement(name = "Bearer Authentication") for a specific controller
@Tag(name = "User")
public class UsersController {

    private final AuthenticationService service;


    @Operation(
            summary = "register a USER",
            description = "Register a user in DB"
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest request
    ){
        return service.register(request);
    }

    @Operation(
            summary = "Confirmation via Email",
            description = "Confirm an account via Email with a confirmation token"
    )
    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token")String confirmationToken) {
        return service.confirmEmail(confirmationToken);
    }

    @Operation(
            summary = "login",
            description = "login of a user and receive a jwt token"
    )
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ){
            return service.authenticate(request);
    }

    @Operation(
            summary = "refresh token",
            description = "it will refresh the token for the user"
    )
    @PostMapping("/refresh-token")
    public void refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    @Operation(
            summary = "update a USER",
            description = "update a user with new information"
    )
    @PutMapping("/update/{userId}")
    @PreAuthorize("@securityService.hasPermission(#request, #userId)")
    public ResponseEntity<?> update(@RequestBody RegisterRequest registerRequest, @PathVariable Integer userId, HttpServletRequest request) {
        service.updateUser(registerRequest, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "delete a USER",
            description = "It will perform a Soft Delete"
    )
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
    @Operation(
            description = "Receive a list of user not deleted only if user had ADMIN role",
            summary = "list of not deleted user"
    )
    public ResponseEntity<?> getUsersTrue() {
        return new ResponseEntity<> (service.findAll(true),HttpStatus.OK);
    }

    @Secured("ADMIN")
    @GetMapping("/getUsersFalse")
    @Operation(
            description = "Receive a list of user deleted only if user had ADMIN role",
            summary = "list of deleted user"
    )
    public ResponseEntity<?> getUsersFalse() {
        return new ResponseEntity<> (service.findAll(false),HttpStatus.OK);
    }
    @GetMapping("/mail")
    @Operation(
            description = "Simple test of email system",
            summary = "email test"
    )
    public ResponseEntity<?> sendMail() throws MessagingException {
        service.triggerMail("AAA","hello from mail!");
        return new ResponseEntity<> (HttpStatus.OK);
    }

    @PostMapping("/forgotPassword")
    @Operation(
            description = "It will send an email with a reset password token",
            summary = "Forgot password system"
    )
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto email) throws MessagingException {
        service.forgotPassword(email.getMail());
        return new ResponseEntity<> (HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    @Operation(
            description = "user give the reset token and the new password",
            summary = "Reset the password"
    )
    public  ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto, @RequestParam(required = false) String token){
        return service.resetPassword(resetPasswordDto, token);
    }
}