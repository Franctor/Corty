package com.corty.backend.services;

import com.corty.backend.dto.CourtAdminResponse;
import com.corty.backend.dto.CourtRequest;
import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.mapper.CourtAdminMapper;
import com.corty.backend.mapper.CourtMapper;
import com.corty.backend.model.Club;
import com.corty.backend.model.Court;
import com.corty.backend.model.Sport;
import com.corty.backend.model.Surface;
import com.corty.backend.repository.ClubRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.SportRepository;
import com.corty.backend.repository.SurfaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtMapper courtMapper;
    private final CourtAdminMapper courtAdminMapper;
    private final ClubRepository clubRepository;
    private final SportRepository sportRepository;
    private final SurfaceRepository surfaceRepository;
    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;

    private static final double DEFAULT_RADIUS_KM = 20.0;
    private static final int DEFAULT_LIMIT = 10;

    public List<NearbyCourtResponse> getNearbyCourts(Double lat, Double lon, String sport) {
        if (lat != null && lon != null) {
            List<Object[]> rows = courtRepository.findNearbyCourtsRaw(
                    lat, lon, DEFAULT_RADIUS_KM, sport, DEFAULT_LIMIT
            );
            return rows.stream()
                    .map(row -> {
                        double distanceKm = ((Number) row[row.length - 1]).doubleValue();
                        Long courtId = ((Number) row[0]).longValue();
                        Court court = courtRepository.findById(courtId).orElseThrow();
                        return courtMapper.fromRaw(row, court, distanceKm);
                    })
                    .toList();
        }
        List<Court> courts = courtRepository.findActiveCourts(
                sport, PageRequest.of(0, DEFAULT_LIMIT)
        );
        return courtMapper.toNearbyCourtList(courts);
    }

    public List<CourtAdminResponse> getAllAdmin() {
        return courtAdminMapper.toResponseList(courtRepository.findAll());
    }

    public CourtAdminResponse getByIdAdmin(Long id) {
        return courtAdminMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public CourtAdminResponse create(CourtRequest request) {
        Court court = courtAdminMapper.toEntity(request);
        court.setClub(findClubOrThrow(request.getClubId()));
        court.setSport(findSportOrThrow(request.getSportId()));
        if (request.getSurfaceId() != null) {
            court.setSurface(findSurfaceOrThrow(request.getSurfaceId()));
        }
        return courtAdminMapper.toResponse(courtRepository.save(court));
    }

    @Transactional
    public CourtAdminResponse update(Long id, CourtRequest request) {
        Court court = findOrThrow(id);
        courtAdminMapper.updateEntity(request, court);
        court.setClub(findClubOrThrow(request.getClubId()));
        court.setSport(findSportOrThrow(request.getSportId()));
        court.setSurface(request.getSurfaceId() != null ? findSurfaceOrThrow(request.getSurfaceId()) : null);
        return courtAdminMapper.toResponse(courtRepository.save(court));
    }

    @Transactional
    public void delete(Long id) {
        Court court = findOrThrow(id);
        if (!court.getBookings().isEmpty()) {
            throw new EntityInUseException("No se puede eliminar la pista porque tiene reservas asociadas");
        }
        courtRepository.delete(court);
    }

    @Transactional
    public void forceDelete(Long id) {
        Court court = findOrThrow(id);
        for (Booking booking : court.getBookings()) {
            playerBookingRepository.deleteAll(booking.getParticipants());
        }
        bookingRepository.deleteAll(court.getBookings());
        courtRepository.delete(court);
    }

    private Court findOrThrow(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pista no encontrada"));
    }

    private Club findClubOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club no encontrado"));
    }

    private Sport findSportOrThrow(Long id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deporte no encontrado"));
    }

    private Surface findSurfaceOrThrow(Long id) {
        return surfaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Superficie no encontrada"));
    }
}
