package com.example.loginmicroservizi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(unique = true)
    private String resetToken;
    private LocalDateTime expireAt;
    @CreationTimestamp
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
