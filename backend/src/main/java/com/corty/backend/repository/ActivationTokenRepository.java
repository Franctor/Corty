package com.corty.backend.repository;

import com.corty.backend.model.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);
    Optional<ActivationToken> findByUser_IdUser(Long userId);
    void deleteByUser_IdUser(Long userId);
}
