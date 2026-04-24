package com.corty.backend.repository;

import com.corty.backend.model.CourtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtScheduleRepository extends JpaRepository<CourtSchedule, Long> {
    List<CourtSchedule> findByCourtIdCourtOrderByDayOfWeek(Long courtId);
    void deleteByCourtIdCourt(Long courtId);
    Optional<CourtSchedule> findByCourtIdCourtAndDayOfWeek(Long courtId, DayOfWeek dayOfWeek);
}
