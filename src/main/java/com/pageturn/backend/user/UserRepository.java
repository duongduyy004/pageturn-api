package com.pageturn.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select u from User u where u.id = :id and u.active = true")
    Optional<User> findActiveById(@Param("id") Long id);

    List<User> findTop10ByActiveTrueAndEmailContainingIgnoreCaseOrderByEmailAsc(String email);
}
