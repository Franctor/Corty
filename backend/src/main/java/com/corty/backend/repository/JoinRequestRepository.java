package com.corty.backend.repository;

import com.corty.backend.model.JoinRequest;
import com.corty.backend.model.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    Optional<JoinRequest> findByBooking_IdBookingAndPlayer_IdPlayer(Long bookingId, Long playerId);

    boolean existsByBooking_IdBookingAndPlayer_IdPlayerAndStatus(Long bookingId, Long playerId, JoinRequestStatus status);

    @Query("SELECT jr FROM JoinRequest jr JOIN FETCH jr.player p JOIN FETCH p.user WHERE jr.booking.idBooking = :bookingId AND jr.status = :status")
    List<JoinRequest> findByBookingIdAndStatus(@Param("bookingId") Long bookingId, @Param("status") JoinRequestStatus status);

    @Query("SELECT jr FROM JoinRequest jr WHERE jr.player.idPlayer = :playerId AND jr.booking.idBooking = :bookingId")
    Optional<JoinRequest> findByPlayerAndBooking(@Param("playerId") Long playerId, @Param("bookingId") Long bookingId);
}
