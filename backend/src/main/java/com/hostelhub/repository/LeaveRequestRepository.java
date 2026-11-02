package com.hostelhub.repository;

import com.hostelhub.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    List<LeaveRequest> findByStudentId(String studentId);
    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);
}
