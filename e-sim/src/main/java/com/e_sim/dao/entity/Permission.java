package com.e_sim.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
// import org.hibernate.annotations.Where;
import org.hibernate.annotations.Where;

import com.e_sim.constant.RoleTypeEnum;

@Getter
@Setter
@Entity
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions")
// @Table(name = "permissions", schema = "esim_db_xsn5")
@Where(clause="deleted=false")
@SQLDelete(sql="UPDATE permissions SET deleted=true WHERE id=?")
public class Permission extends BaseEntity {

    @Column(length=100, unique=true, nullable=false)
    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 6)
    private RoleTypeEnum type = RoleTypeEnum.USER;

    private String description;
}
