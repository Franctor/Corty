package com.corty.backend.repository;

import com.corty.backend.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByUser_IdUser(Long userId);
    boolean existsByCif(String cif);
    boolean existsByBusinessName(String businessName);
}
