package com.example.loginmicroservizi.repository;

import com.example.loginmicroservizi.model.ResetPsw;
import com.example.loginmicroservizi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResetPswRepository extends JpaRepository<ResetPsw, Long> {
    ResetPsw findResetPswByResetToken(String token);

    List<ResetPsw> findAllByUser(User user);
}
