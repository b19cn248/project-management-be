package com.ptit.projectmanagementbe.repository;

import com.ptit.projectmanagementbe.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    Optional<Project> findByUuid(String uuid);

    @Query("SELECT p FROM Project p WHERE p.uuid = :uuid AND p.isDeleted = false")
    Optional<Project> findActiveByUuid(@Param("uuid") String uuid);

    Page<Project> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.userId = :userId AND p.status = :status AND p.isDeleted = false")
    Page<Project> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status, Pageable pageable);

    boolean existsByNameAndUserIdAndIsDeletedFalse(String name, Integer userId);

    @Query("SELECT p FROM Project p WHERE p.userId = :userId AND p.isDeleted = false ORDER BY p.endDate ASC")
    List<Project> findActiveProjectsByUserIdOrderByEndDate(@Param("userId") Integer userId);
}
