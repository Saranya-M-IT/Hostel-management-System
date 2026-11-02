package com.hostelhub.repository;

import com.hostelhub.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUser(com.hostelhub.entity.User user);
    Boolean existsByEmail(String email);
    List<Student> findByAssignedRoomId(String roomId);
    List<Student> findByAllocationStatus(String allocationStatus);
}
