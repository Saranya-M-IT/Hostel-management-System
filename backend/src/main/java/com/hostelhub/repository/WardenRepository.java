package com.hostelhub.repository;

import com.hostelhub.entity.Warden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardenRepository extends JpaRepository<Warden, String> {
    Optional<Warden> findByEmail(String email);
    Optional<Warden> findByUser(com.hostelhub.entity.User user);
    Boolean existsByEmail(String email);
    List<Warden> findByAssignedBlock(String assignedBlock);
}
