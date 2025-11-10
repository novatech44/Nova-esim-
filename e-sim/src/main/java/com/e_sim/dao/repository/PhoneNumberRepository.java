package com.e_sim.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.e_sim.dao.entity.PhoneNumber;
import com.e_sim.dao.entity.User;

import java.util.Optional;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {
  boolean existsByNumberAndUser(String number, User user);
  Optional<PhoneNumber> findByIdAndUser(Long id, User user);
}