package com.banco.anteojos.backend.persistence.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.security.entities.User;

public interface UserPostgresSqlRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
}
