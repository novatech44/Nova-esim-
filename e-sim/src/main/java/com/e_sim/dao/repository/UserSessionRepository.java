package com.e_sim.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.e_sim.dao.entity.UserSessionEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, String> {

    @Query("SELECT s FROM UserSessionEntity s WHERE s.expiryTime > :now")
    List<UserSessionEntity> findActiveSessionsAfter(@Param("now") Instant now);

    @Query("SELECT s FROM UserSessionEntity s WHERE s.principalName = :principalName AND s.expiryTime > :now")
    List<UserSessionEntity> findActiveSessionsByPrincipal(@Param("principalName") String principalName, @Param("now") Instant now);

    @Query("SELECT COUNT(s) FROM UserSessionEntity s WHERE s.expiryTime > :now")
    Long countActiveSessions(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM UserSessionEntity s WHERE s.expiryTime < :now")
    int deleteExpiredSessions(@Param("now") Instant now);

    @Query("SELECT s FROM UserSessionEntity s WHERE s.lastAccessTime < :threshold AND s.expiryTime > :now")
    List<UserSessionEntity> findInactiveSessions(@Param("threshold") Instant threshold, @Param("now") Instant now);

    Optional<UserSessionEntity> findBySessionId(String sessionId);
}