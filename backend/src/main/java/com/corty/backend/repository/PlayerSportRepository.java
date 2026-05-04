package com.corty.backend.repository;

import com.corty.backend.model.PlayerSport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerSportRepository extends JpaRepository<PlayerSport, Long> {

    Optional<PlayerSport> findByPlayer_IdPlayerAndSport_IdSport(Long playerId, Long sportId);
}
