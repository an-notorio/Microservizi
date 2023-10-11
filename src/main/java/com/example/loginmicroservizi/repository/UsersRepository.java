package com.example.loginmicroservizi.repository;

import com.example.loginmicroservizi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);


    User findByUserId(Integer id);
}
