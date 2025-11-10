package com.e_sim.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.e_sim.constant.RoleTypeEnum;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Table(name = "roles", schema = "esim")
@Where(clause="deleted=false")
@SQLDelete(sql="UPDATE roles SET deleted=true WHERE id=?")
public class Role extends BaseEntity {

    @Column(length = 30, unique = true, nullable = false)
    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 6)
    private RoleTypeEnum type = RoleTypeEnum.USER;

    @Builder.Default
    @Column(name = "default_role")
    private boolean defaultRole = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ToString.Exclude
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

}