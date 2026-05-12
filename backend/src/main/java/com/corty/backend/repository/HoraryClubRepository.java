package com.corty.backend.repository;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.HoraryClub;

@Repository
public interface HoraryClubRepository extends JpaRepository<HoraryClub, Long> {

    Optional<HoraryClub> findByClub_IdClubAndDayWeek(Long clubId, DayOfWeek dayWeek);
}
