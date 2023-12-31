package com.example.loginmicroservizi.repository;

import com.example.loginmicroservizi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    void deleteByUserId(Integer userId);
    Boolean existsByEmail(String email);
    User findByUserId(Integer id);
    List<User> findAllByEmail(String email);
}
