package com.e_sim.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signup_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private String phoneNumber;

    private String otp;
    private LocalDateTime otpExpiresAt;

    @Builder.Default
    private boolean verified = false;

    private LocalDateTime createdAt;
}