package com.example.loginmicroservizi.repository;

import com.example.loginmicroservizi.model.ResetPsw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPswRepository extends JpaRepository<ResetPsw, Long> {
    ResetPsw findResetPswByResetToken(String token);
}
