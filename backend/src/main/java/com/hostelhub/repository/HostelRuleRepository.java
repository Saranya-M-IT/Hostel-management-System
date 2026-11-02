package com.hostelhub.repository;

import com.hostelhub.entity.HostelRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelRuleRepository extends JpaRepository<HostelRule, String> {
    List<HostelRule> findByActive(boolean active);
}
