package com.hostelhub.service;

import com.hostelhub.dto.HostelRuleDTO;
import com.hostelhub.entity.HostelRule;
import com.hostelhub.exception.ResourceNotFoundException;
import com.hostelhub.repository.HostelRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HostelRuleService {

    @Autowired
    private HostelRuleRepository hostelRuleRepository;

    public List<HostelRuleDTO> getAllRules() {
        return hostelRuleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<HostelRuleDTO> getActiveRules() {
        return hostelRuleRepository.findByActive(true).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public HostelRuleDTO addRule(HostelRuleDTO dto) {
        long count = hostelRuleRepository.count();
        String ruleId = String.format("RUL-%d", count + 1);

        HostelRule rule = new HostelRule(
                ruleId,
                dto.getTitle(),
                dto.getCategory(),
                dto.getDescription(),
                dto.isActive()
        );

        rule = hostelRuleRepository.save(rule);
        return mapToDTO(rule);
    }

    @Transactional
    public HostelRuleDTO updateRule(String id, HostelRuleDTO dto) {
        HostelRule rule = hostelRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with ID: " + id));

        if (dto.getTitle() != null) rule.setTitle(dto.getTitle());
        if (dto.getCategory() != null) rule.setCategory(dto.getCategory());
        if (dto.getDescription() != null) rule.setDescription(dto.getDescription());
        rule.setActive(dto.isActive());

        rule = hostelRuleRepository.save(rule);
        return mapToDTO(rule);
    }

    @Transactional
    public HostelRuleDTO toggleRule(String id) {
        HostelRule rule = hostelRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with ID: " + id));

        rule.setActive(!rule.isActive());
        rule = hostelRuleRepository.save(rule);
        return mapToDTO(rule);
    }

    @Transactional
    public void deleteRule(String id) {
        HostelRule rule = hostelRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with ID: " + id));

        hostelRuleRepository.delete(rule);
    }

    public HostelRuleDTO mapToDTO(HostelRule r) {
        HostelRuleDTO dto = new HostelRuleDTO();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setCategory(r.getCategory());
        dto.setDescription(r.getDescription());
        dto.setActive(r.isActive());
        return dto;
    }
}
