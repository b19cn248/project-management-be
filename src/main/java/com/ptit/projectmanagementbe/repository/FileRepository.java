package com.ptit.projectmanagementbe.repository;

import com.ptit.projectmanagementbe.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {

    Optional<File> findByUuid(String uuid);

    @Query("SELECT f FROM File f WHERE f.uuid = :uuid AND f.isDeleted = false")
    Optional<File> findActiveByUuid(@Param("uuid") String uuid);

    List<File> findByTaskId(Integer taskId);

    List<File> findByProjectId(Integer projectId);

    @Query("SELECT f FROM File f WHERE f.taskId = :taskId AND f.isDeleted = false")
    Page<File> findActiveByTaskId(@Param("taskId") Integer taskId, Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.projectId = :projectId AND f.isDeleted = false")
    Page<File> findActiveByProjectId(@Param("projectId") Integer projectId, Pageable pageable);

    @Query("SELECT f FROM File f " +
            "WHERE (f.taskId IN (SELECT t.id FROM Task t WHERE t.projectId IN " +
            "(SELECT p.id FROM Project p WHERE p.userId = :userId)) " +
            "OR f.projectId IN (SELECT p.id FROM Project p WHERE p.userId = :userId)) " +
            "AND f.isDeleted = false")
    Page<File> findByUserId(@Param("userId") Integer userId, Pageable pageable);
}
