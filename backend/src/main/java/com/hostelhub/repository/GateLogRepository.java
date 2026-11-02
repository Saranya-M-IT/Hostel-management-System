package com.hostelhub.repository;

import com.hostelhub.entity.GateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GateLogRepository extends JpaRepository<GateLog, Long> {

    List<GateLog> findByStudentIdOrderByScanTimeDesc(String studentId);

    Optional<GateLog> findFirstByStudentIdOrderByScanTimeDesc(String studentId);

    List<GateLog> findByWardenIdOrderByScanTimeDesc(String wardenId);

    List<GateLog> findAllByOrderByScanTimeDesc();

    @Query("SELECT g FROM GateLog g WHERE " +
           "(:studentId IS NULL OR LOWER(g.studentId) LIKE LOWER(CONCAT('%', :studentId, '%')) OR LOWER(g.studentName) LIKE LOWER(CONCAT('%', :studentId, '%'))) AND " +
           "(:roomNumber IS NULL OR LOWER(g.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%'))) AND " +
           "(:logDate IS NULL OR g.logDate = :logDate) AND " +
           "(:entryType IS NULL OR g.type = :entryType) " +
           "ORDER BY g.scanTime DESC")
    List<GateLog> filterLogs(
            @Param("studentId") String studentId,
            @Param("roomNumber") String roomNumber,
            @Param("logDate") LocalDate logDate,
            @Param("entryType") GateLog.LogType entryType
    );

    @Query("SELECT g FROM GateLog g WHERE " +
           "(g.wardenId = :wardenId OR :wardenId IS NULL) AND " +
           "(:studentId IS NULL OR LOWER(g.studentId) LIKE LOWER(CONCAT('%', :studentId, '%')) OR LOWER(g.studentName) LIKE LOWER(CONCAT('%', :studentId, '%'))) AND " +
           "(:roomNumber IS NULL OR LOWER(g.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%'))) AND " +
           "(:logDate IS NULL OR g.logDate = :logDate) AND " +
           "(:entryType IS NULL OR g.type = :entryType) " +
           "ORDER BY g.scanTime DESC")
    List<GateLog> filterWardenLogs(
            @Param("wardenId") String wardenId,
            @Param("studentId") String studentId,
            @Param("roomNumber") String roomNumber,
            @Param("logDate") LocalDate logDate,
            @Param("entryType") GateLog.LogType entryType
    );
}
