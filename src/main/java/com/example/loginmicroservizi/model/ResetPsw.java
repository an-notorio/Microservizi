package com.example.loginmicroservizi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reset")
public class ResetPsw {

    @Id
    @GeneratedValue
    private Long idReset;
    private String resetToken;
    //TODO Need to be rewrite when used
    private LocalDateTime expireAt;
    private Timestamp timeStamp;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName ="user_id")
    private User user;

    @Transient
    private boolean isExpired;

    public boolean isExpired() {
        return getExpireAt().isBefore(LocalDateTime.now());
    }
}
