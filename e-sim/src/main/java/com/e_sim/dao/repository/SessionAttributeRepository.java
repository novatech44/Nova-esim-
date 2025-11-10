package com.e_sim.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.e_sim.dao.entity.SessionAttributeEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionAttributeRepository extends JpaRepository<SessionAttributeEntity, Long> {

    @Query("SELECT a FROM SessionAttributeEntity a WHERE a.session.sessionId = :sessionId")
    List<SessionAttributeEntity> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT a FROM SessionAttributeEntity a WHERE a.session.sessionId = :sessionId AND a.attributeName = :attributeName")
    Optional<SessionAttributeEntity> findBySessionIdAndAttributeName(@Param("sessionId") String sessionId, @Param("attributeName") String attributeName);

    @Query("SELECT COUNT(a) FROM SessionAttributeEntity a WHERE a.session.sessionId = :sessionId")
    Long countAttributesBySessionId(@Param("sessionId") String sessionId);
}