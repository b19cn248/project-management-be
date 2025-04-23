package com.ptit.projectmanagementbe.repository;

import com.ptit.projectmanagementbe.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    Optional<Schedule> findByUuid(String uuid);

    @Query("SELECT s FROM Schedule s WHERE s.uuid = :uuid AND s.isDeleted = false")
    Optional<Schedule> findActiveByUuid(@Param("uuid") String uuid);

    Page<Schedule> findByUserId(Integer userId, Pageable pageable);

    List<Schedule> findByTaskId(Integer taskId);

    List<Schedule> findByMeetingId(Integer meetingId);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.scheduleDate = :date AND s.isDeleted = false " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findByUserIdAndScheduleDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.scheduleDate BETWEEN :startDate AND :endDate " +
            "AND s.isDeleted = false ORDER BY s.scheduleDate ASC, s.startTime ASC")
    List<Schedule> findByUserIdAndScheduleDateBetween(@Param("userId") Integer userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Schedule s " +
            "WHERE s.userId = :userId AND s.scheduleDate = :date " +
            "AND ((s.startTime <= :endTime AND s.endTime >= :startTime)) " +
            "AND s.id <> :excludeId AND s.isDeleted = false")
    boolean existsOverlappingSchedule(@Param("userId") Integer userId,
                                      @Param("date") LocalDate date,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime,
                                      @Param("excludeId") Integer excludeId);

    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId AND s.scheduleDate = :date " +
            "AND s.isDeleted = false ORDER BY s.startTime ASC")
    List<Schedule> findFreeTimeSlots(@Param("userId") Integer userId, @Param("date") LocalDate date);
}
