package com.e_sim.dao.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.e_sim.dao.entity.SignupRequest;

@Repository
public interface SignupRequestRepository extends JpaRepository<SignupRequest, Long> {

    Optional<SignupRequest> findByEmail(String email);

    Optional<SignupRequest> findByEmailAndOtp(String email, String otp);

    boolean existsByEmail(String email);

    List<SignupRequest> findAllByCreatedAtBefore(LocalDateTime cutoff);
}