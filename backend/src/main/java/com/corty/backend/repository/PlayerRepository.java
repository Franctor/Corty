package com.corty.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUser_IdUser(Long userId);

    Optional<Player> findByUser_Username(String username);

    @Query("""
        SELECT p FROM Player p
        WHERE p.user.enabled = true
          AND p.user.username != :currentUsername
          AND (LOWER(p.user.username) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.name)          LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.surname)       LIKE LOWER(CONCAT('%', :q, '%')))
          AND NOT EXISTS (
            SELECT f FROM Friendship f
            WHERE (f.requester.username = :currentUsername AND f.recipient = p.user)
               OR (f.recipient.username = :currentUsername AND f.requester = p.user)
          )
        ORDER BY p.user.username ASC
        LIMIT 10
    """)
    List<Player> searchPlayers(@Param("q") String q, @Param("currentUsername") String currentUsername);
}
