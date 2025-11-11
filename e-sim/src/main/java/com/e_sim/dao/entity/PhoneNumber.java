package com.e_sim.dao.entity;

// import com.traverse.authenticationservice.util.NigerianPhoneNetworkDetector;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.Objects;

import com.e_sim.util.NigerianPhoneNetworkDetector;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Table(name = "phone_numbers")
public class PhoneNumber extends BaseEntity {


    @Column(nullable = false, length = 11 , unique = true)
    private String number;

    private String network;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public PhoneNumber(String number) {
        this.number = number;
        this.network = NigerianPhoneNetworkDetector.detectNetwork(number);
        this.active = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber that)) return false;
        return Objects.equals(number, that.number) &&
                Objects.equals(user != null ? user.getId() : null,
                        that.user != null ? that.user.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, user != null ? user.getId() : null);
    }
}
