package com.corty.backend.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.corty.backend.dto.SlotResponse;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Court;
import com.corty.backend.model.CourtBlock;
import com.corty.backend.model.CourtSchedule;
import com.corty.backend.model.HoraryClub;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.CourtBlockRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.CourtScheduleRepository;
import com.corty.backend.repository.HoraryClubRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourtAvailabilityService {

    private final CourtRepository courtRepository;
    private final CourtScheduleRepository courtScheduleRepository;
    private final CourtBlockRepository courtBlockRepository;
    private final BookingRepository bookingRepository;
    private final HoraryClubRepository horaryClubRepository;

    public List<SlotResponse> getAvailableSlots(Long courtId, LocalDate date) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Pista no encontrada"));

        DayOfWeek dow = date.getDayOfWeek();

        // Obtener horario base del día
        LocalTime openTime;
        LocalTime closeTime;

        if (court.isUseClubSchedule()) {
            Optional<HoraryClub> schedule = horaryClubRepository
                    .findByClub_IdClubAndDayWeek(court.getClub().getIdClub(), dow);
            if (schedule.isEmpty() || schedule.get().isClosed()) {
                return List.of();
            }
            openTime = schedule.get().getOpenTime();
            closeTime = schedule.get().getCloseTime();
        } else {
            Optional<CourtSchedule> schedule = courtScheduleRepository
                    .findByCourtIdCourtAndDayOfWeek(courtId, dow);
            if (schedule.isEmpty() || schedule.get().isClosed()) {
                return List.of();
            }
            openTime = schedule.get().getOpenTime();
            closeTime = schedule.get().getCloseTime();
        }

        // Obtener bloqueos y reservas del día
        List<CourtBlock> blocks = courtBlockRepository.findByCourtIdCourtAndBlockDate(courtId, date);
        List<Booking> bookings = bookingRepository.findByCourtAndDate(courtId, date);

        int slotMinutes = court.getSlotDurationMinutes();
        List<SlotResponse> slots = new ArrayList<>();
        LocalTime current = openTime;
        boolean isToday = date.isEqual(java.time.LocalDate.now());
        LocalTime now = LocalTime.now();

        while (!current.plusMinutes(slotMinutes).isAfter(closeTime)) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);
            boolean available = (!isToday || current.isAfter(now))
                    && isSlotAvailable(current, slotEnd, blocks, bookings);
            slots.add(new SlotResponse(current, slotEnd, available));
            current = slotEnd;
        }

        return slots;
    }

    private boolean isSlotAvailable(LocalTime start, LocalTime end,
            List<CourtBlock> blocks, List<Booking> bookings) {
        for (CourtBlock block : blocks) {
            if (start.isBefore(block.getEndTime()) && end.isAfter(block.getStartTime())) {
                return false;
            }
        }
        for (Booking booking : bookings) {
            if (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime())) {
                return false;
            }
        }
        return true;
    }
}
