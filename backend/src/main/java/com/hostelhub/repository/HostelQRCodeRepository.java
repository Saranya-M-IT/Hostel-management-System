package com.hostelhub.repository;

import com.hostelhub.entity.HostelQRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostelQRCodeRepository extends JpaRepository<HostelQRCode, Long> {
    Optional<HostelQRCode> findFirstByStatusOrderByCreatedAtDesc(String status);
    Optional<HostelQRCode> findByQrTokenAndStatus(String qrToken, String status);
}
