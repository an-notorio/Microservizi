package com.example.loginmicroservizi.service;

import com.example.loginmicroservizi.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SecurityService {
    @Autowired
    private UsersRepository usersRepository;

    public boolean hasPermission(HttpServletRequest request, Integer userId) {
        if(Objects.equals(usersRepository.findAllByEmail(request.getUserPrincipal().getName()).get(0).getUserId(), userId)) {
            return true;
        }
        else {
            return false;
        }

    }
}