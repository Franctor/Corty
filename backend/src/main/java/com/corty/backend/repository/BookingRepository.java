package com.corty.backend.repository;

import com.corty.backend.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b "
            + "WHERE b.court.idCourt = :courtId "
            + "AND b.date = :date "
            + "AND b.bookingStatus NOT IN ('CANCELLED', 'PENDING_PAYMENT') "
            + "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsOverlappingBooking(
            @Param("courtId") Long courtId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.court.idCourt = :courtId AND b.date = :date AND b.bookingStatus <> 'CANCELLED'")
    List<Booking> findByCourtAndDate(@Param("courtId") Long courtId, @Param("date") LocalDate date);

    @Query("""
                        SELECT b FROM Booking b
                        LEFT JOIN b.participants pb
                        WHERE (b.owner.idUser = :userId OR pb.player.idPlayer = :playerId)
                          AND b.date >= :today
                          AND b.bookingStatus = 'CONFIRMED'
                        ORDER BY b.date ASC, b.startTime ASC
                        """)
    List<Booking> findUpcomingByUserOrPlayer(
            @Param("userId") Long userId,
            @Param("playerId") Long playerId,
            @Param("today") LocalDate today);

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
    Page<Booking> findAllFilteredByOrg(@Param("orgId") Long orgId, @Param("search") String search,
            Pageable pageable);

    @Query("""
                        SELECT DISTINCT b FROM Booking b
                        JOIN FETCH b.court c
                        JOIN FETCH c.club cl
                        JOIN FETCH c.sport sp
                        LEFT JOIN FETCH b.participants
                        WHERE (b.owner.idUser = :userId
                           OR EXISTS (
                               SELECT 1 FROM PlayerBooking pb
                               WHERE pb.booking = b AND pb.player.user.idUser = :userId
                           ))
                          AND (:#{#statuses.size()} = 0 OR b.bookingStatus IN :statuses)
                        ORDER BY b.date DESC, b.startTime DESC
                        """)
    List<Booking> findByUserAndStatuses(
            @Param("userId") Long userId,
            @Param("statuses") java.util.Collection<com.corty.backend.model.enums.BookingStatus> statuses);

    @Query(value = """
                        SELECT b.id_booking,
                               (6371 * ACOS(LEAST(1.0,
                                   COS(RADIANS(:lat)) * COS(RADIANS(cl.geo_lat))
                                   * COS(RADIANS(cl.geo_long) - RADIANS(:lon))
                                   + SIN(RADIANS(:lat)) * SIN(RADIANS(cl.geo_lat))
                               ))) AS distance_km,
                               COALESCE((SELECT AVG(ps.level)
                                          FROM player_bookings pb2
                                          JOIN player_sports ps ON ps.id_player = pb2.id_player
                                          JOIN sports s2 ON s2.id_sport = ps.id_sport
                                          WHERE pb2.id_booking = b.id_booking
                                            AND s2.id_sport = c.id_sport), 0) AS avg_level
                        FROM bookings b
                        JOIN courts c ON b.id_court = c.id_court
                        JOIN clubs cl ON c.id_club = cl.id_club
                        JOIN sports s ON c.id_sport = s.id_sport
                        WHERE b.booking_type = 'PUBLIC'
                          AND b.booking_status = 'CONFIRMED'
                          AND (b.date > CURDATE() OR (b.date = CURDATE() AND b.start_time > CURTIME()))
                          AND (SELECT COUNT(*) FROM player_bookings pb WHERE pb.id_booking = b.id_booking) < s.players_per_match
                          AND (:sportName IS NULL OR LOWER(s.name) = LOWER(:sportName))
                          AND (:dateFrom IS NULL OR b.date >= :dateFrom)
                          AND (:dateTo IS NULL OR b.date <= :dateTo)
                          AND (:levelMin IS NULL OR COALESCE((SELECT AVG(ps2.level)
                                FROM player_bookings pb4
                                JOIN player_sports ps2 ON ps2.id_player = pb4.id_player
                                JOIN sports s3 ON s3.id_sport = ps2.id_sport
                                WHERE pb4.id_booking = b.id_booking AND s3.id_sport = c.id_sport), 0) >= :levelMin)
                          AND (:levelMax IS NULL OR COALESCE((SELECT AVG(ps3.level)
                                FROM player_bookings pb5
                                JOIN player_sports ps3 ON ps3.id_player = pb5.id_player
                                JOIN sports s4 ON s4.id_sport = ps3.id_sport
                                WHERE pb5.id_booking = b.id_booking AND s4.id_sport = c.id_sport), 0) <= :levelMax)
                          AND b.id_user != :userId
                          AND NOT EXISTS (SELECT 1 FROM player_bookings pb3 WHERE pb3.id_booking = b.id_booking AND pb3.id_player = :playerId)
                          AND ((:lat IS NULL OR :lon IS NULL) OR
                               (6371 * ACOS(LEAST(1.0,
                                   COS(RADIANS(:lat)) * COS(RADIANS(cl.geo_lat))
                                   * COS(RADIANS(cl.geo_long) - RADIANS(:lon))
                                   + SIN(RADIANS(:lat)) * SIN(RADIANS(cl.geo_lat))
                               ))) <= :radiusKm)
                        ORDER BY distance_km ASC, avg_level ASC
                        LIMIT :limitCount
                        """, nativeQuery = true)
    List<Object[]> findPublicBookingsRaw(
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            @Param("radiusKm") double radiusKm,
            @Param("sportName") String sportName,
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo,
            @Param("levelMin") Double levelMin,
            @Param("levelMax") Double levelMax,
            @Param("limitCount") int limitCount,
            @Param("userId") Long userId,
            @Param("playerId") Long playerId);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' AND (b.date < :today OR (b.date = :today AND b.endTime <= :nowTime))")
    List<Booking> findConfirmedPastEndTime(@Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime);

    /**
     * splitPayment=true: ingresos brutos por mes — [year, month, sum]
     */
    @Query("""
                SELECT YEAR(b.date), MONTH(b.date),
                       COALESCE(SUM(COALESCE(pb.paidAmount, pb.splitPrice)), 0)
                FROM Booking b
                JOIN b.participants pb
                WHERE b.court.club.idClub = :clubId
                  AND b.bookingStatus = 'COMPLETED'
                  AND b.splitPayment = true
                GROUP BY YEAR(b.date), MONTH(b.date)
                ORDER BY YEAR(b.date), MONTH(b.date)
                """)
    List<Object[]> revenueByMonthSplit(@Param("clubId") Long clubId);

    /**
     * splitPayment=false: ingresos brutos por mes — [year, month, sum]
     */
    @Query("""
                SELECT YEAR(b.date), MONTH(b.date), COALESCE(SUM(b.totalPrice), 0)
                FROM Booking b
                WHERE b.court.club.idClub = :clubId
                  AND b.bookingStatus = 'COMPLETED'
                  AND b.splitPayment = false
                GROUP BY YEAR(b.date), MONTH(b.date)
                ORDER BY YEAR(b.date), MONTH(b.date)
                """)
    List<Object[]> revenueByMonthNoSplit(@Param("clubId") Long clubId);

    @Query("""
                SELECT COALESCE(SUM(COALESCE(pb.paidAmount, pb.splitPrice)), 0)
                FROM Booking b JOIN b.participants pb
                WHERE b.court.club.idClub = :clubId
                  AND b.bookingStatus = 'COMPLETED'
                  AND b.splitPayment = true
                """)
    java.math.BigDecimal totalRevenueSplit(@Param("clubId") Long clubId);

    @Query("""
                SELECT COALESCE(SUM(b.totalPrice), 0)
                FROM Booking b
                WHERE b.court.club.idClub = :clubId
                  AND b.bookingStatus = 'COMPLETED'
                  AND b.splitPayment = false
                """)
    java.math.BigDecimal totalRevenueNoSplit(@Param("clubId") Long clubId);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status AND b.createdAt < :cutoff")
    List<Booking> findExpiredByStatus(
            @Param("status") BookingStatus status,
            @Param("cutoff") LocalDateTime cutoff);

    @Query("""
                        SELECT b FROM Booking b
                        JOIN b.participants pb
                        WHERE pb.player.idPlayer = :playerId
                          AND b.bookingStatus = 'COMPLETED'
                        ORDER BY b.date DESC, b.startTime DESC
                        """)
    List<Booking> findRecentCompletedByPlayer(
            @Param("playerId") Long playerId,
            org.springframework.data.domain.Pageable pageable);
}
