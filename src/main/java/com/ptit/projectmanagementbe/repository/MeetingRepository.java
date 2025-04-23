package com.ptit.projectmanagementbe.repository;

import com.ptit.projectmanagementbe.entity.Meeting;
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
public interface MeetingRepository extends JpaRepository<Meeting, Integer> {

    Optional<Meeting> findByUuid(String uuid);

    @Query("SELECT m FROM Meeting m WHERE m.uuid = :uuid AND m.isDeleted = false")
    Optional<Meeting> findActiveByUuid(@Param("uuid") String uuid);

    Page<Meeting> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT m FROM Meeting m WHERE m.userId = :userId AND m.meetingDate = :date AND m.isDeleted = false " +
            "ORDER BY m.startTime ASC")
    List<Meeting> findByUserIdAndMeetingDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

    @Query("SELECT m FROM Meeting m WHERE m.userId = :userId AND m.meetingDate BETWEEN :startDate AND :endDate " +
            "AND m.isDeleted = false ORDER BY m.meetingDate ASC, m.startTime ASC")
    List<Meeting> findByUserIdAndMeetingDateBetween(@Param("userId") Integer userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Meeting m " +
            "WHERE m.userId = :userId AND m.meetingDate = :date " +
            "AND ((m.startTime <= :endTime AND m.endTime >= :startTime)) " +
            "AND m.id <> :excludeId AND m.isDeleted = false")
    boolean existsOverlappingMeeting(@Param("userId") Integer userId,
                                     @Param("date") LocalDate date,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime,
                                     @Param("excludeId") Integer excludeId);
}
