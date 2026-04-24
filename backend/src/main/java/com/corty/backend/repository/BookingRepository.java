package com.corty.backend.repository;

import com.corty.backend.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.court.idCourt = :courtId " +
            "AND b.date = :date " +
            "AND b.bookingStatus <> 'CANCELLED' " +
            "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsOverlappingBooking(
            @Param("courtId") Long courtId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT b FROM Booking b WHERE b.court.idCourt = :courtId AND b.date = :date AND b.bookingStatus <> 'CANCELLED'")
    List<Booking> findByCourtAndDate(@Param("courtId") Long courtId, @Param("date") LocalDate date);

    // Próxima reserva: la más cercana en el futuro donde el jugador participa o es propietario
    @Query("""
            SELECT b FROM Booking b
            LEFT JOIN b.participants pb
            WHERE (b.owner.idUser = :userId OR pb.player.idPlayer = :playerId)
              AND b.date >= :today
              AND b.bookingStatus IN ('PENDING', 'CONFIRMED')
            ORDER BY b.date ASC, b.startTime ASC
            """)
    List<Booking> findUpcomingByUserOrPlayer(
            @Param("userId") Long userId,
            @Param("playerId") Long playerId,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE (:search IS NULL OR :search = ''
              OR LOWER(b.court.club.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.court.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.owner.username) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.bookingStatus) LIKE LOWER(CONCAT('%', :search, '%'))
              OR CAST(b.date AS string) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY b.date DESC, b.startTime DESC
            """)
    Page<Booking> findAllFiltered(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.court.club.organization.idOrganization = :orgId
              AND (:search IS NULL OR :search = ''
              OR LOWER(b.court.club.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.court.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.owner.username) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(b.bookingStatus) LIKE LOWER(CONCAT('%', :search, '%'))
              OR CAST(b.date AS string) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY b.date DESC, b.startTime DESC
            """)
    Page<Booking> findAllFilteredByOrg(@Param("orgId") Long orgId, @Param("search") String search, Pageable pageable);

    // Actividad reciente: últimas N reservas completadas donde el jugador participó
    @Query("""
            SELECT b FROM Booking b
            JOIN b.participants pb
            WHERE pb.player.idPlayer = :playerId
              AND b.bookingStatus = 'COMPLETED'
            ORDER BY b.date DESC, b.startTime DESC
            """)
    List<Booking> findRecentCompletedByPlayer(
            @Param("playerId") Long playerId,
            org.springframework.data.domain.Pageable pageable
    );
}
