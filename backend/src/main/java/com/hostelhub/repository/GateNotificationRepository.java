package com.hostelhub.repository;

import com.hostelhub.entity.GateNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GateNotificationRepository extends JpaRepository<GateNotification, Long> {
    List<GateNotification> findByRecipientIdOrderByTimestampDesc(String recipientId);
    List<GateNotification> findByRecipientRoleOrderByTimestampDesc(String recipientRole);
}
