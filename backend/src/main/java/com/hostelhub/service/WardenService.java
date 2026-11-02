package com.hostelhub.service;

import com.hostelhub.dto.WardenDTO;
import com.hostelhub.entity.User;
import com.hostelhub.entity.Warden;
import com.hostelhub.exception.BadRequestException;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.UserRepository;
import com.hostelhub.repository.WardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WardenService {

    @Autowired
    private WardenRepository wardenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<WardenDTO> getAllWardens() {
        return wardenRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public WardenDTO getWardenById(String id) {
        Warden warden = wardenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warden officer not found with ID: " + id));
        return mapToDTO(warden);
    }

    public WardenDTO getWardenByUsername(String username) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseGet(() -> userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username)));

        Warden warden = wardenRepository.findByUser(user)
                .orElseGet(() -> wardenRepository.findByEmail(username.toLowerCase())
                .orElseGet(() -> wardenRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Warden officer not found for user: " + username))));

        return mapToDTO(warden);
    }

    @Transactional
    public WardenDTO addWarden(WardenDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (wardenRepository.existsByEmail(email) || userRepository.existsByUsername(email)) {
            throw new BadRequestException("Warden user with this email already exists!");
        }

        String password = (dto.getPassword() != null && !dto.getPassword().isBlank())
                ? dto.getPassword()
                : "warden123";

        User user = new User(email, passwordEncoder.encode(password), User.Role.WARDEN);
        user = userRepository.save(user);

        long maxId = 200;
        for (Warden w : wardenRepository.findAll()) {
            if (w.getId() != null && w.getId().toUpperCase().startsWith("WRD-")) {
                try {
                    long num = Long.parseLong(w.getId().substring(4));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String wardenId = String.format("WRD-%d", maxId + 1);

        String phone = dto.getPhone() != null ? dto.getPhone().replaceAll("[^0-9]", "") : "";

        Warden warden = new Warden(wardenId, dto.getName().trim(), email, phone, dto.getBlock().trim());
        warden.setUser(user);
        warden = wardenRepository.save(warden);

        return mapToDTO(warden);
    }

    @Transactional
    public WardenDTO updateWarden(String id, WardenDTO dto) {
        Warden warden = wardenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warden officer not found with ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) warden.setName(dto.getName().trim());
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) warden.setPhone(dto.getPhone().replaceAll("[^0-9]", ""));
        if (dto.getBlock() != null && !dto.getBlock().isBlank()) warden.setAssignedBlock(dto.getBlock().trim());

        warden = wardenRepository.save(warden);
        return mapToDTO(warden);
    }

    @Transactional
    public void deleteWarden(String id) {
        Warden warden = wardenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warden officer not found with ID: " + id));

        User user = warden.getUser();
        wardenRepository.delete(warden);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public WardenDTO mapToDTO(Warden w) {
        WardenDTO dto = new WardenDTO();
        dto.setId(w.getId());
        dto.setName(w.getName());
        dto.setBlock(w.getAssignedBlock());
        dto.setEmail(w.getEmail());
        dto.setPhone(w.getPhone());
        dto.setStatus("Active");
        return dto;
    }
}
