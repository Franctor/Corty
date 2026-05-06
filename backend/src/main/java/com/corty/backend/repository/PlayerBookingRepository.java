package com.corty.backend.repository;

import com.corty.backend.model.PlayerBooking;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerBookingRepository extends JpaRepository<PlayerBooking, Long> {

    @Query("SELECT pb FROM PlayerBooking pb WHERE pb.booking.idBooking = :bookingId AND pb.player.idPlayer = :playerId")
    Optional<PlayerBooking> findByBookingIdAndPlayerId(
            @Param("bookingId") Long bookingId,
            @Param("playerId") Long playerId
    );

    @Modifying
    @Query("DELETE FROM PlayerBooking pb WHERE pb.booking.idBooking = :bookingId AND pb.player.idPlayer = :playerId")
    void deleteByBookingIdAndPlayerId(
            @Param("bookingId") Long bookingId,
            @Param("playerId") Long playerId
    );

    @Modifying
    @Query("DELETE FROM PlayerBooking pb WHERE pb.player.idPlayer = :playerId")
    void deleteAllByPlayerId(@Param("playerId") Long playerId);

    @Query("""
        SELECT pb FROM PlayerBooking pb
        JOIN FETCH pb.booking b
        JOIN FETCH b.court c
        JOIN FETCH c.sport s
        WHERE pb.player.idPlayer = :playerId
          AND b.bookingStatus = 'COMPLETED'
          AND b.result IS NOT NULL
    """)
    List<PlayerBooking> findCompletedWithResultByPlayerId(@Param("playerId") Long playerId);
}
