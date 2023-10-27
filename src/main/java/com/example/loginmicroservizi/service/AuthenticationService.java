package com.example.loginmicroservizi.service;

import com.example.loginmicroservizi.dto.*;
import com.example.loginmicroservizi.model.ConfirmationToken;
import com.example.loginmicroservizi.model.ResetPsw;
import com.example.loginmicroservizi.model.Role;
import com.example.loginmicroservizi.model.User;
import com.example.loginmicroservizi.repository.ConfirmationTokenRepository;
import com.example.loginmicroservizi.repository.ResetPswRepository;
import com.example.loginmicroservizi.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository repository;
    private final ResetPswRepository resetPswRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EmailSenderService senderService;
    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;


    // CONFIRMATION EMAIL
    public ResponseEntity<?> register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .attempts(0)
                .status(true)
                .build();
        repository.save(user);
        ConfirmationToken confirmationToken = new ConfirmationToken(user);
        confirmationTokenRepository.save(confirmationToken);
        senderService.sendSimpleEmail(user.getEmail(), "Complete Registration!", "To confirm your account, please click here : "
                +"http://localhost:8081/api/confirm-account?token="+confirmationToken.getConfirmationToken());
        System.out.println("Confirmation Token: " + confirmationToken.getConfirmationToken());
        return ResponseEntity.ok("Verify email by the link sent on your email address");
    }
    public ResponseEntity<?> confirmEmail(String confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
        if(token != null)
        {
            User user = repository.findAllByEmail(token.getUser().getEmail()).get(0);
            user.setEnabled(true);
            repository.save(user);
            return ResponseEntity.ok("Email verified successfully!");
        }
        return ResponseEntity.badRequest().body("Error: Couldn't verify email");
    }

    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        if (user.isStatus() && user.isEnabled()) {
            try{
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                    )
                );}
            catch (Exception e){
                user.setAttempts(user.getAttempts()+1);
                if(user.getAttempts()==3){
                    user.setStatus(false);
                }
                repository.save(user);
                return ResponseEntity.ok("Error - Wrong Password");
            }
            user.setAttempts(0);
            repository.save(user);
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build());
        } else {
            if(user.getAttempts()==3){
                return new ResponseEntity<>("Error - max attempts reached - User Disabled", HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>("User is disabled", HttpStatus.FORBIDDEN);
        }
    }

    public void updateUser(RegisterRequest request, Integer userId) {
        // Retrieve the original user from the repository
        Optional<User> userOptional = repository.findById(userId);
        if (userOptional.isPresent()) {
            User originalUser = userOptional.get();
            String firstName = (request.getFirstName() != null && !request.getFirstName().isEmpty()) ? request.getFirstName() : originalUser.getFirstName();
            String lastName = (request.getLastName() != null && !request.getLastName().isEmpty()) ? request.getLastName() : originalUser.getLastName();
            String email = (request.getEmail() != null && !request.getEmail().isEmpty()) ? request.getEmail() : originalUser.getEmail();
            String password = (request.getPassword() != null && !request.getPassword().isEmpty()) ? passwordEncoder.encode(request.getPassword()) : originalUser.getPassword();
            List<Role> role = ((request.getRole() != null && !request.getRole().isEmpty()) ? request.getRole() : originalUser.getRole());
            var updatedUser = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(password)
                    .role(role)
                    .userId(userId)
                    .status(true)
                    .build();
            repository.save(updatedUser);
        }
    }

    public void deleteUser(Integer userId) {
        repository.deleteById(userId);
    }

    public UserDto getUser(Integer userId){
        User user = repository.findByUserId(userId);
        var userToShow = UserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        return userToShow;
    }

    public Iterable<User> findAll(boolean isDeleted){
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedUserFilter");
        filter.setParameter("isDeleted", isDeleted);
        Iterable<User> products =  repository.findAll();
        session.disableFilter("deletedUserFilter");
        return products;
    }

    //SEND MAIL
    public void triggerMail(String mail, String token) throws MessagingException {
        //TODO change "example@mail.com" to mail for send mail to the correct user
        senderService.sendSimpleEmail(mail,
                "Reset password",
                token);
    }

    public void forgotPassword(String email) throws MessagingException {
        User user = repository.findAllByEmail(email).get(0);
        String token = jwtService.generateTokenResetPsw(user);
        String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8081")
                .path("/api/resetPassword").queryParam("token", token).toUriString();
        triggerMail(email, url);
        var resetPsw = ResetPsw.builder()
                .resetToken(token);
    }

    public ResponseEntity<?> resetPassword(ResetPasswordDto resetPasswordDto, String token){
        User user = repository.findAllByEmail(jwtService.extractUsername(token)).get(0);
        ResetPsw resetPsw = resetPswRepository.findResetPswByResetToken(token);
        if(!resetPsw.isExpired()){
            if(jwtService.isTokenValid(token,user)){
                if(resetPasswordDto.getPassword().equals(resetPasswordDto.getRepeatPassword())){
                    user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
                    repository.save(user);
                    resetPsw.setExpireAt(LocalDateTime.now());
                    resetPswRepository.save(resetPsw);
                    return new ResponseEntity<> (HttpStatus.OK);
                }
                else{
                    return new ResponseEntity<> ("Password Incorrect",HttpStatus.BAD_REQUEST);
                }
            }
            else{
                return new ResponseEntity<> ("Token Invalid",HttpStatus.FORBIDDEN);
            }
        }
        else{
            return new ResponseEntity<> ("Token expired",HttpStatus.FORBIDDEN);
        }
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail!= null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if(jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}