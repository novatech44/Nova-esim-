package com.e_sim.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_sessions")
public class UserSessionEntity {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "principal_name", length = 100)
    private String principalName;

    @Column(name = "creation_time", nullable = false)
    private Instant creationTime;

    @Column(name = "last_access_time", nullable = false)
    private Instant lastAccessTime;

    @Column(name = "max_inactive_interval", nullable = false)
    private int maxInactiveInterval;

    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SessionAttributeEntity> attributes;

    // Constructors
    public UserSessionEntity() {
        this.creationTime = Instant.now();
        this.lastAccessTime = this.creationTime;
    }

    public UserSessionEntity(String sessionId, int maxInactiveInterval) {
        this();
        this.sessionId = sessionId;
        this.maxInactiveInterval = maxInactiveInterval;
        this.expiryTime = this.lastAccessTime.plusSeconds(maxInactiveInterval);
    }

    public void setLastAccessTime(Instant lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
        this.expiryTime = lastAccessTime.plusSeconds(maxInactiveInterval);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }
}
