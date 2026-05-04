package com.corty.backend.repository;

import com.corty.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUser_IdUser(Long userId);
    Optional<Player> findByUser_Username(String username);
}
