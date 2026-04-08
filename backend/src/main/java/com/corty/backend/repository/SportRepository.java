package com.corty.backend.repository;

import com.corty.backend.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {

    // Deportes ordenados por número de partidas completadas (PlayerBooking COUNT)
    @Query("""
            SELECT s FROM Sport s
            LEFT JOIN Court c ON c.sport = s
            LEFT JOIN Booking b ON b.court = c AND b.bookingStatus = 'COMPLETED'
            LEFT JOIN PlayerBooking pb ON pb.booking = b
            GROUP BY s
            ORDER BY COUNT(pb) DESC
            """)
    List<Sport> findTopByPopularity(org.springframework.data.domain.Pageable pageable);
}
