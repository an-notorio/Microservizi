package com.example.loginmicroservizi.service;

import com.example.loginmicroservizi.dto.AuthenticationRequest;
import com.example.loginmicroservizi.dto.AuthenticationResponse;
import com.example.loginmicroservizi.dto.RegisterRequest;
import com.example.loginmicroservizi.dto.UserDto;
import com.example.loginmicroservizi.model.Role;
import com.example.loginmicroservizi.model.User;
import com.example.loginmicroservizi.repository.UsersRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private EntityManager entityManager;

    public ResponseEntity<?> register(RegisterRequest request) {
        if(repository.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.ok("Email already used");
        }else {
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
            var jwtToken = jwtService.generateToken(user);
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build());
        }
    }

    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        if(user.getAttempts()<3) {
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
            if (user.isStatus()) {
                repository.save(user);
                var jwtToken = jwtService.generateToken(user);
                return ResponseEntity.ok(AuthenticationResponse.builder()
                        .token(jwtToken)
                        .build());
            } else {
                return ResponseEntity.ok("User is disabled");
            }
        }else{
            return ResponseEntity.ok("Error - max attempts reached - User Disabled");
        }
    }

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
}
