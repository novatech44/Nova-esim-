package com.e_sim.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "session_attributes")
public class SessionAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_name", nullable = false, length = 200)
    private String attributeName;

    @Lob
    @Column(name = "attribute_value", nullable = false)
    private byte[] attributeValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private UserSessionEntity session;

    // Constructors
    public SessionAttributeEntity() {
    }

    public SessionAttributeEntity(String attributeName, byte[] attributeValue, UserSessionEntity session) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.session = session;
    }
}
