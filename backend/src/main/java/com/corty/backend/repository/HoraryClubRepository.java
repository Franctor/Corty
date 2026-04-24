package com.corty.backend.repository;

import com.corty.backend.model.HoraryClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.Optional;

@Repository
public interface HoraryClubRepository extends JpaRepository<HoraryClub, Long> {
    Optional<HoraryClub> findByClub_IdClubAndDayWeek(Long clubId, DayOfWeek dayWeek);
}
