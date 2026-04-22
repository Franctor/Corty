package com.corty.backend.repository;

import com.corty.backend.model.CourtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtScheduleRepository extends JpaRepository<CourtSchedule, Long> {
    List<CourtSchedule> findByCourtIdCourtOrderByDayOfWeek(Long courtId);
    void deleteByCourtIdCourt(Long courtId);
}
