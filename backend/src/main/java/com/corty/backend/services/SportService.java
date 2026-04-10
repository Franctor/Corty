package com.corty.backend.services;

import com.corty.backend.dto.SportFilterResponse;
import com.corty.backend.dto.SportRequest;
import com.corty.backend.dto.SportResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.SportMapper;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Court;
import com.corty.backend.model.Sport;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.SportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService {

    private static final int FILTER_LIMIT = 5;

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;

    public List<SportFilterResponse> getTopSportsForFilter() {
        return sportMapper.toFilterResponseList(
                sportRepository.findTopByPopularity(PageRequest.of(0, FILTER_LIMIT))
        );
    }

    public List<SportResponse> getAll() {
        return sportMapper.toResponseList(sportRepository.findAll());
    }

    public SportResponse getById(Long id) {
        return sportMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public SportResponse create(SportRequest request) {
        Sport sport = sportMapper.toEntity(request);
        return sportMapper.toResponse(sportRepository.save(sport));
    }

    @Transactional
    public SportResponse update(Long id, SportRequest request) {
        Sport sport = findOrThrow(id);
        sportMapper.updateEntity(request, sport);
        return sportMapper.toResponse(sportRepository.save(sport));
    }

    @Transactional
    public void delete(Long id) {
        Sport sport = findOrThrow(id);
        if (!sport.getCourts().isEmpty()) {
            throw new EntityInUseException("No se puede eliminar el deporte porque tiene pistas asociadas");
        }
        sportRepository.delete(sport);
    }

    @Transactional
    public void forceDelete(Long id) {
        Sport sport = findOrThrow(id);
        for (Court court : sport.getCourts()) {
            for (Booking booking : court.getBookings()) {
                playerBookingRepository.deleteAll(booking.getParticipants());
            }
            bookingRepository.deleteAll(court.getBookings());
        }
        courtRepository.deleteAll(sport.getCourts());
        sportRepository.delete(sport);
    }

    private Sport findOrThrow(Long id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deporte no encontrado"));
    }
}
