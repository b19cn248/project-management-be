package com.ptit.projectmanagementbe.repository;

import com.ptit.projectmanagementbe.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Optional<Task> findByUuid(String uuid);

    @Query("SELECT t FROM Task t WHERE t.uuid = :uuid AND t.isDeleted = false")
    Optional<Task> findActiveByUuid(@Param("uuid") String uuid);

    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId AND t.isDeleted = false")
    Page<Task> findByProjectId(Integer projectId, Pageable pageable);

    @Query("SELECT t FROM Task t JOIN Project p ON t.projectId = p.id " +
            "WHERE p.userId = :userId AND t.isDeleted = false " +
            "ORDER BY CASE t.priority " +
            "WHEN 'HIGH' THEN 1 " +
            "WHEN 'MEDIUM' THEN 2 " +
            "WHEN 'LOW' THEN 3 " +
            "ELSE 4 END, t.endDate ASC")
    List<Task> findActiveTasksByUserIdOrderByPriorityAndEndDate(@Param("userId") Integer userId);

    @Query("SELECT t FROM Task t WHERE t.projectId = :projectId " +
            "AND t.status = :status AND t.isDeleted = false")
    Page<Task> findByProjectIdAndStatus(@Param("projectId") Integer projectId, @Param("status") String status, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.endDate BETWEEN :startDate AND :endDate " +
            "AND t.projectId IN (SELECT p.id FROM Project p WHERE p.userId = :userId) " +
            "AND t.isDeleted = false")
    List<Task> findTasksDueBetweenDates(@Param("userId") Integer userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.isDeleted = false")
    Long countByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.status = 'COMPLETED' AND t.isDeleted = false")
    Long countCompletedTasksByProjectId(@Param("projectId") Integer projectId);
}
