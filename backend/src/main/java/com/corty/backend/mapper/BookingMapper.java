package com.corty.backend.mapper;

import com.corty.backend.dto.BookingDetailResponse;
import com.corty.backend.dto.NextBookingResponse;
import com.corty.backend.dto.RecentActivityResponse;
import com.corty.backend.model.Booking;
import com.corty.backend.model.PlayerBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", source = "idBooking")
    @Mapping(target = "courtName", source = "court.name")
    @Mapping(target = "clubName", source = "court.club.name")
    @Mapping(target = "clubAddress", source = "court.club.address")
    @Mapping(target = "clubLogoUrl", source = "court.club.logoUrl")
    @Mapping(target = "clubLat", source = "court.club.geoLat")
    @Mapping(target = "clubLng", source = "court.club.geoLong")
    @Mapping(target = "sport", source = "court.sport.name")
    @Mapping(target = "sportIconUrl", source = "court.sport.iconUrl")
    @Mapping(target = "bookingType", source = "bookingType", qualifiedByName = "enumToString")
    @Mapping(target = "bookingStatus", source = "bookingStatus", qualifiedByName = "enumToString")
    @Mapping(target = "paymentMethod", source = "paymentMethod", qualifiedByName = "enumToString")
    @Mapping(target = "courtPrice", source = "totalPrice")
    @Mapping(target = "hasWinners", source = "booking", qualifiedByName = "hasWinners")
    @Mapping(target = "fullyPaid", source = "fullyPaid")
    @Mapping(target = "splitPayment", source = "splitPayment")
    @Mapping(target = "currentUserOwner", ignore = true)
    @Mapping(target = "participants", ignore = true)
    BookingDetailResponse toBookingDetailResponse(Booking booking);

    @Mapping(target = "playerId", source = "player.idPlayer")
    @Mapping(target = "name", source = "player.name")
    @Mapping(target = "surname", source = "player.surname")
    @Mapping(target = "avatarUrl", source = "player.avatarUrl")
    @Mapping(target = "team", source = "team", qualifiedByName = "enumToString")
    @Mapping(target = "confirmed", source = "confirmed")
    @Mapping(target = "winner", source = "winner")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "currentUser", ignore = true)
    BookingDetailResponse.ParticipantResponse toParticipantResponse(PlayerBooking pb);

    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e == null ? null : e.name();
    }

    @Named("hasWinners")
    default boolean hasWinners(Booking booking) {
        final boolean result;
        if (booking.getParticipants() == null) {
            result = false;
        } else {
            result = booking.getParticipants().stream().anyMatch(PlayerBooking::isWinner);
        }
        return result;
    }

    @Mapping(target = "id", source = "idBooking")
    @Mapping(target = "courtLabel", source = "court.name")
    @Mapping(target = "clubName", source = "court.club.name")
    @Mapping(target = "time", source = "startTime")
    @Mapping(target = "sport", source = "court.sport.name")
    @Mapping(target = "confirmedPlayers", source = "booking", qualifiedByName = "countConfirmed")
    @Mapping(target = "totalPlayers", source = "court.sport.playersPerMatch")
    NextBookingResponse toNextBookingResponse(Booking booking);

    @Mapping(target = "id", source = "idBooking")
    @Mapping(target = "sportId", source = "court.sport.idSport")
    @Mapping(target = "sport", source = "court.sport.name")
    @Mapping(target = "sportIconUrl", source = "court.sport.iconUrl")
    @Mapping(target = "sportColor", source = "court.sport.color")
    @Mapping(target = "description", source = "booking", qualifiedByName = "buildDescription")
    @Mapping(target = "date", source = "booking", qualifiedByName = "buildDateTime")
    RecentActivityResponse toRecentActivityResponse(Booking booking);

    List<RecentActivityResponse> toRecentActivityList(List<Booking> bookings);

    @Named("countConfirmed")
    default int countConfirmed(Booking booking) {
        final int confirmedCount;
        if (booking.getParticipants() == null) {
            confirmedCount = 0;
        } else {
            confirmedCount = (int) booking.getParticipants().stream()
                    .filter(PlayerBooking::isConfirmed)
                    .count();
        }
        return confirmedCount;
    }

    @Named("buildDescription")
    default String buildDescription(Booking booking) {
        String sport = booking.getCourt().getSport().getName();
        String club = booking.getCourt().getClub().getName();
        return sport + " · " + club;
    }

    @Named("buildDateTime")
    default LocalDateTime buildDateTime(Booking booking) {
        return LocalDateTime.of(booking.getDate(), booking.getStartTime());
    }
}
