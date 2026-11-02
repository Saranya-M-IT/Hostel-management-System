package com.hostelhub.repository;

import com.hostelhub.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {
    List<Complaint> findByStudentId(String studentId);
    List<Complaint> findByStatus(Complaint.ComplaintStatus status);
}
