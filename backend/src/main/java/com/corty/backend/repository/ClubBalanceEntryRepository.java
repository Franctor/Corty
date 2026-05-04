package com.corty.backend.repository;

import com.corty.backend.model.ClubBalanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ClubBalanceEntryRepository extends JpaRepository<ClubBalanceEntry, Long> {

    List<ClubBalanceEntry> findByClub_IdClubOrderByCreatedAtDesc(Long clubId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ClubBalanceEntry e WHERE e.club.idClub = :clubId")
    BigDecimal sumByClub(@Param("clubId") Long clubId);

    /** Penalizaciones agrupadas por año-mes: [[year, month, sum], ...] */
    @Query("""
        SELECT YEAR(e.createdAt), MONTH(e.createdAt), COALESCE(SUM(e.amount), 0)
        FROM ClubBalanceEntry e
        WHERE e.club.idClub = :clubId
        GROUP BY YEAR(e.createdAt), MONTH(e.createdAt)
        ORDER BY YEAR(e.createdAt), MONTH(e.createdAt)
        """)
    List<Object[]> sumByMonth(@Param("clubId") Long clubId);

    /** Penalizaciones agrupadas por motivo: [[reason, sum], ...] */
    @Query("""
        SELECT e.reason, COALESCE(SUM(e.amount), 0)
        FROM ClubBalanceEntry e
        WHERE e.club.idClub = :clubId
        GROUP BY e.reason
        """)
    List<Object[]> sumByReason(@Param("clubId") Long clubId);
}
