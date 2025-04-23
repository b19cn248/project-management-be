package com.ptit.projectmanagementbe.repository;

import com.ptit.projectmanagementbe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUuid(String uuid);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.uuid = :uuid AND u.isDeleted = false")
    Optional<User> findActiveByUuid(@Param("uuid") String uuid);
}
