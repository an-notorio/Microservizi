package com.example.loginmicroservizi.service;

import com.example.loginmicroservizi.common.RoleName;
import com.example.loginmicroservizi.dto.AuthenticationRequest;
import com.example.loginmicroservizi.dto.AuthenticationResponse;
import com.example.loginmicroservizi.dto.RegisterRequest;
import com.example.loginmicroservizi.model.Role;
import com.example.loginmicroservizi.model.User;
import com.example.loginmicroservizi.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(true)
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

   /* public void updateUser(RegisterRequest request, Integer userId) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .id(userId)
                .status(true)
                .build();
        repository.save(user);
    }*/

    public void updateUser(RegisterRequest request, Integer userId) {
        // Recupera l'utente originale dal repository
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
        User user = repository.findByUserId(userId);
        user.setStatus(false);
        repository.save(user);
    }
}
