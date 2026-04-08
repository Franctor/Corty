package com.corty.backend.repository;

import com.corty.backend.model.PlayerBooking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerBookingRepository extends JpaRepository<PlayerBooking, Long> {

    @Query("SELECT pb FROM PlayerBooking pb WHERE pb.booking.idBooking = :bookingId AND pb.player.idPlayer = :playerId")
    Optional<PlayerBooking> findByBookingIdAndPlayerId(
            @Param("bookingId") Long bookingId,
            @Param("playerId") Long playerId
    );
}
