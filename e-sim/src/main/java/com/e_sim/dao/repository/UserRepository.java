package com.e_sim.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.e_sim.dao.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUsernameOrEmail(String username, String email);

  @Query("SELECT u FROM User u WHERE u.email = :usernameOrEmail OR u.username = :usernameOrEmail")
  Optional<User> findByEmailOrUsername(@Param("usernameOrEmail") String usernameOrEmail);
}
