package com.corty.backend.repository;

import com.corty.backend.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findByOrganization_IdOrganization(Long organizationId);
}
