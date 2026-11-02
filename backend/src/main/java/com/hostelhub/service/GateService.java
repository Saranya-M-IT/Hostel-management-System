package com.hostelhub.service;

import com.hostelhub.dto.GateLogDTO;
import com.hostelhub.dto.GateScanResponse;
import com.hostelhub.entity.*;
import com.hostelhub.exception.BadRequestException;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GateService {

    public static final String COMMON_QR_TOKEN = "HOSTEL-HUB-COMMON-GATE-QR-2026";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private HostelQRCodeRepository qrCodeRepository;

    @Autowired
    private GateLogRepository gateLogRepository;

    @Autowired
    private GateNotificationRepository notificationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WardenRepository wardenRepository;

    public String getActiveCommonQRCodeToken() {
        Optional<HostelQRCode> qrOpt = qrCodeRepository.findFirstByStatusOrderByCreatedAtDesc("ACTIVE");
        if (qrOpt.isPresent()) {
            return qrOpt.get().getQrToken();
        }
        HostelQRCode newQr = new HostelQRCode(COMMON_QR_TOKEN, "ACTIVE");
        return qrCodeRepository.save(newQr).getQrToken();
    }

    @Transactional
    public GateScanResponse processScan(String qrToken, String usernameOrEmail) {
        String activeToken = getActiveCommonQRCodeToken();
        if (!activeToken.equalsIgnoreCase(qrToken.trim())) {
            throw new BadRequestException("Invalid or unrecognized Hostel QR Code scan.");
        }

        // Identify currently authenticated student
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByUsername(usernameOrEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOrEmail)));

        Student student = studentRepository.findByUser(user)
                .orElseGet(() -> studentRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + usernameOrEmail)));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // Check latest scan for auto IN/OUT toggle and cooldown
        Optional<GateLog> latestLogOpt = gateLogRepository.findFirstByStudentIdOrderByScanTimeDesc(student.getId());
        GateLog.LogType nextType = GateLog.LogType.IN;

        if (latestLogOpt.isPresent()) {
            GateLog latest = latestLogOpt.get();
            // 30-second duplicate scan cooldown check
            if (latest.getScanTime() != null && latest.getScanTime().plusSeconds(30).isAfter(now)) {
                throw new BadRequestException("Duplicate scan detected. Please wait at least 30 seconds between scans.");
            }
            if (latest.getType() == GateLog.LogType.IN) {
                nextType = GateLog.LogType.OUT;
            } else {
                nextType = GateLog.LogType.IN;
            }
        }

        String roomNo = "Unassigned";
        String wardenId = "WRD-GENERAL";

        if (student.getAssignedRoom() != null) {
            roomNo = student.getAssignedRoom().getId();
            String block = student.getAssignedRoom().getBlock();
            if (block != null) {
                List<Warden> wardens = wardenRepository.findByAssignedBlock(block);
                if (!wardens.isEmpty()) {
                    wardenId = wardens.get(0).getId();
                }
            }
        }

        String parentContact = student.getEmergencyContact() != null ? student.getEmergencyContact() : "Not Provided";

        GateLog newLog = new GateLog(
                student.getId(),
                student.getFullName(),
                today,
                now,
                nextType,
                roomNo,
                wardenId,
                parentContact
        );

        GateLog savedLog = gateLogRepository.save(newLog);

        String timeStr = now.format(TIME_FORMATTER);
        String dateStr = now.format(DATE_FORMATTER);
        String statusAction = nextType == GateLog.LogType.IN ? "entered" : "left";

        // 1. Warden Notification
        String wardenMsg = String.format("Student %s (%s) has %s the hostel at %s on %s. Room: %s",
                student.getFullName(), student.getId(), statusAction, timeStr, dateStr, roomNo);
        notificationRepository.save(new GateNotification("WARDEN", wardenId, student.getId(), student.getFullName(), wardenMsg, nextType.name()));

        // 2. Parent Notification
        String parentMsg = String.format("Student %s (%s) has %s the hostel at %s on %s.",
                student.getFullName(), student.getId(), statusAction, timeStr, dateStr);
        notificationRepository.save(new GateNotification("PARENT", parentContact, student.getId(), student.getFullName(), parentMsg, nextType.name()));

        String resultMsg = String.format("%s recorded successfully at %s", nextType.name(), timeStr);

        return new GateScanResponse(
                true,
                nextType.name(),
                timeStr,
                resultMsg,
                mapToDTO(savedLog)
        );
    }

    public List<GateLogDTO> getStudentLogs(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByUsername(usernameOrEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOrEmail)));

        Student student = studentRepository.findByUser(user)
                .orElseGet(() -> studentRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + usernameOrEmail)));

        return gateLogRepository.findByStudentIdOrderByScanTimeDesc(student.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<GateLogDTO> getWardenLogs(String wardenUsername, String studentFilter, String roomFilter, LocalDate dateFilter, GateLog.LogType typeFilter) {
        String wardenId = null;
        Optional<User> uOpt = userRepository.findByUsername(wardenUsername);
        if (uOpt.isPresent()) {
            Optional<Warden> wOpt = wardenRepository.findByUser(uOpt.get());
            if (wOpt.isPresent()) {
                wardenId = wOpt.get().getId();
            }
        }

        return gateLogRepository.filterWardenLogs(wardenId, studentFilter, roomFilter, dateFilter, typeFilter)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<GateLogDTO> getAdminLogs(String studentFilter, String roomFilter, LocalDate dateFilter, GateLog.LogType typeFilter) {
        return gateLogRepository.filterLogs(studentFilter, roomFilter, dateFilter, typeFilter)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private GateLogDTO mapToDTO(GateLog log) {
        String timeFormatted = log.getScanTime() != null ? log.getScanTime().format(TIME_FORMATTER) : "";
        String dateFormatted = log.getLogDate() != null ? log.getLogDate().format(DATE_FORMATTER) : "";
        return new GateLogDTO(
                log.getId(),
                log.getStudentId(),
                log.getStudentName(),
                log.getLogDate(),
                log.getScanTime(),
                timeFormatted,
                dateFormatted,
                log.getType() != null ? log.getType().name() : "IN",
                log.getRoomNumber(),
                log.getWardenId(),
                log.getParentContact()
        );
    }
}
