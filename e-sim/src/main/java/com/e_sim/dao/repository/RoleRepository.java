package com.e_sim.dao.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.e_sim.config.CaffeineCacheConfiguration;
import com.e_sim.dao.entity.Role;

import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Cacheable(
            value = CaffeineCacheConfiguration.DEFAULT_ROLES_CACHE,
            key = "{#root.targetClass.simpleName}",
            unless = "#result.empty"
    )
    @Query("SELECT r FROM Role r WHERE r.defaultRole = true")
    Set<Role> findDefaultRoles();
}
