package com.corty.backend.repository;

import com.corty.backend.model.CourtBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtBlockRepository extends JpaRepository<CourtBlock, Long> {
    List<CourtBlock> findByCourtIdCourtOrderByBlockDateAscStartTimeAsc(Long courtId);
}
