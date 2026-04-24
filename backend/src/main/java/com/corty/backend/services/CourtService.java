package com.corty.backend.services;

import com.corty.backend.dto.CourtAdminResponse;
import com.corty.backend.dto.CourtRequest;
import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.dto.CourtDetailResponse;
import com.corty.backend.mapper.CourtAdminMapper;
import com.corty.backend.mapper.CourtDetailMapper;
import com.corty.backend.mapper.CourtMapper;
import com.corty.backend.model.Club;
import com.corty.backend.model.Court;
import com.corty.backend.model.Sport;
import com.corty.backend.model.Surface;
import com.corty.backend.model.Organization;
import com.corty.backend.model.User;
import com.corty.backend.repository.ClubRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.SportRepository;
import com.corty.backend.repository.SurfaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtMapper courtMapper;
    private final CourtAdminMapper courtAdminMapper;
    private final CourtDetailMapper courtDetailMapper;
    private final ClubRepository clubRepository;
    private final OrganizationRepository organizationRepository;
    private final SportRepository sportRepository;
    private final SurfaceRepository surfaceRepository;
    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;

    private static final double DEFAULT_RADIUS_KM = 20.0;
    private static final double EXPLORE_RADIUS_KM = 5000.0;
    private static final int DEFAULT_LIMIT = 50;

    public List<NearbyCourtResponse> getNearbyCourts(
            Double lat, Double lon, String sport, String surface,
            Boolean covered, Boolean lighting,
            java.math.BigDecimal maxPrice, String sortBy, String sortDir,
            Double radiusKm) {
        boolean coveredOnly  = Boolean.TRUE.equals(covered);
        boolean lightingOnly = Boolean.TRUE.equals(lighting);
        String resolvedSortBy  = sortBy  != null ? sortBy  : "distance";
        String resolvedSortDir = sortDir != null ? sortDir : "asc";
        double resolvedRadius  = radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM;
        final List<NearbyCourtResponse> result;
        if (lat != null && lon != null) {
            List<Object[]> rows = courtRepository.findNearbyCourtsRaw(
                    lat, lon, resolvedRadius, sport, surface,
                    coveredOnly, lightingOnly, maxPrice,
                    resolvedSortBy, resolvedSortDir, DEFAULT_LIMIT
            );
            result = rows.stream()
                    .map(row -> {
                        Long courtId = ((Number) row[0]).longValue();
                        double distanceKm = ((Number) row[1]).doubleValue();
                        Court court = courtRepository.findByIdWithDetails(courtId).orElseThrow();
                        return courtMapper.fromRaw(row, court, distanceKm);
                    })
                    .toList();
        } else {
            boolean desc = "desc".equalsIgnoreCase(resolvedSortDir);
            String sortField = "price".equals(resolvedSortBy) ? "pricePerHour" : "name";
            org.springframework.data.domain.Sort sort = desc
                    ? org.springframework.data.domain.Sort.by(sortField).descending()
                    : org.springframework.data.domain.Sort.by(sortField).ascending();
            List<Court> courts = courtRepository.findActiveCourts(
                    sport, surface, coveredOnly, lightingOnly, maxPrice,
                    PageRequest.of(0, DEFAULT_LIMIT, sort)
            );
            result = courtMapper.toNearbyCourtList(courts);
        }
        return result;
    }

    public Page<CourtAdminResponse> getAllAdmin(int page, int size, String search, User principal) {
        PageRequest pageable = PageRequest.of(page, size);
        boolean isOrg = principal.getRole() != null && "ORGANIZATION".equals(principal.getRole().getName());
        if (isOrg) {
            Organization org = organizationRepository.findByUser_IdUser(principal.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada"));
            return courtRepository.findAllFilteredByOrg(org.getIdOrganization(), search, pageable)
                    .map(courtAdminMapper::toResponse);
        }
        return courtRepository.findAllFiltered(search, pageable)
                .map(courtAdminMapper::toResponse);
    }

    public CourtAdminResponse getByIdAdmin(Long id) {
        return courtAdminMapper.toResponse(findOrThrow(id));
    }

    public CourtDetailResponse getByIdForPlayer(Long id) {
        return courtDetailMapper.toResponse(courtRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pista no encontrada")));
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
