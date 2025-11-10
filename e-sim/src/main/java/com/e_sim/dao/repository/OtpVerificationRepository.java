package com.e_sim.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.e_sim.dao.entity.OtpVerification;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long>{

    @Modifying
    @Query("DELETE FROM OtpVerification e WHERE e.expiresAt < :now")
    int deleteAllByExpiresAtBefore(@Param("now")LocalDateTime now);

    void deleteAllByEmailAndVerifiedFalse(String email);

    boolean existsByExpiresAtBefore(LocalDateTime now);

    Optional<OtpVerification> findFirstByOtpCodeAndVerifiedFalseOrderByExpiresAtAsc(String otpCode);

    Optional<OtpVerification> findByOtpCodeAndEmailAndVerifiedFalse(String otp, String email);

    Optional<OtpVerification> findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(String email);

    void deleteAllByEmail(String email);
       
}
