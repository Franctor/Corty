package com.corty.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.corty.backend.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByUser_IdUser(Long userId);

    boolean existsByCif(String cif);

    boolean existsByBusinessName(String businessName);
}
