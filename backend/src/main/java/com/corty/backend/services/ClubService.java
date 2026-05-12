package com.corty.backend.services;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corty.backend.dto.ClubBalanceEntryResponse;
import com.corty.backend.dto.ClubRequest;
import com.corty.backend.dto.ClubResponse;
import com.corty.backend.dto.ClubStatsResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.ClubMapper;
import com.corty.backend.model.Booking;
import com.corty.backend.model.City;
import com.corty.backend.model.Club;
import com.corty.backend.model.Court;
import com.corty.backend.model.Organization;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.ClubBalanceReason;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.ClubBalanceEntryRepository;
import com.corty.backend.repository.ClubRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.PlayerBookingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final CityRepository cityRepository;
    private final OrganizationRepository organizationRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final ClubBalanceEntryRepository clubBalanceEntryRepository;

    public List<ClubResponse> getAll() {
        return clubMapper.toResponseList(clubRepository.findAll());
    }

    public ClubResponse getById(Long id) {
        return clubMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ClubResponse create(ClubRequest request, User principal) {
        Club club = clubMapper.toEntity(request);
        club.setCity(findCityOrThrow(request.getCityId()));
        club.setOrganization(resolveOrganization(request, principal));
        return clubMapper.toResponse(clubRepository.save(club));
    }

    @Transactional
    public ClubResponse update(Long id, ClubRequest request, User principal) {
        Club club = findOrThrow(id);
        clubMapper.updateEntity(request, club);
        club.setCity(findCityOrThrow(request.getCityId()));
        club.setOrganization(resolveOrganization(request, principal));
        return clubMapper.toResponse(clubRepository.save(club));
    }

    private Organization resolveOrganization(ClubRequest request, User principal) {
        boolean isOrg = principal.getRole() != null && "ORGANIZATION".equals(principal.getRole().getName());
        if (isOrg) {
            return organizationRepository.findByUser_IdUser(principal.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada"));
        }
        return findOrganizationOrThrow(request.getOrganizationId());
    }

    @Transactional
    public void delete(Long id) {
        Club club = findOrThrow(id);
        if (!club.getCourts().isEmpty()) {
            throw new EntityInUseException("No se puede eliminar el club porque tiene pistas asociadas");
        }
        clubRepository.delete(club);
    }

    @Transactional
    public void forceDelete(Long id) {
        Club club = findOrThrow(id);
        for (Court court : club.getCourts()) {
            for (Booking booking : court.getBookings()) {
                playerBookingRepository.deleteAll(booking.getParticipants());
            }
            bookingRepository.deleteAll(court.getBookings());
        }
        courtRepository.deleteAll(club.getCourts());
        clubRepository.delete(club);
    }

    private Club findOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club no encontrado"));
    }

    private City findCityOrThrow(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada"));
    }

    private Organization findOrganizationOrThrow(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getByOrganizationId(Long orgId) {
        return clubRepository.findByOrganization_IdOrganization(orgId)
                .stream()
                .map(clubMapper::toResponse)
                .toList();
    }

    private static final BigDecimal CORTY_KEEP = new BigDecimal("0.95");

    @Transactional(readOnly = true)
    public ClubStatsResponse getStats(Long clubId) {
        BigDecimal totalRevenue = bookingRepository.totalRevenueSplit(clubId)
                .add(bookingRepository.totalRevenueNoSplit(clubId))
                .multiply(CORTY_KEEP);
        BigDecimal totalPenalties = clubBalanceEntryRepository.sumByClub(clubId);

        Map<String, BigDecimal> revenueByMonth = mergeMaps(
                toMap(bookingRepository.revenueByMonthSplit(clubId)),
                toMap(bookingRepository.revenueByMonthNoSplit(clubId)));

        revenueByMonth.replaceAll((k, v) -> v.multiply(CORTY_KEEP));
        Map<String, BigDecimal> penaltiesByMonth = toMap(clubBalanceEntryRepository.sumByMonth(clubId));

        Map<String, BigDecimal> penaltiesByReason = new LinkedHashMap<>();
        for (Object[] row : clubBalanceEntryRepository.sumByReason(clubId)) {
            penaltiesByReason.put(((ClubBalanceReason) row[0]).name(), (BigDecimal) row[1]);
        }

        List<ClubBalanceEntryResponse> entries = clubBalanceEntryRepository
                .findByClub_IdClubOrderByCreatedAtDesc(clubId)
                .stream()
                .map(ClubBalanceEntryResponse::from)
                .toList();

        return new ClubStatsResponse(
                totalRevenue, totalPenalties,
                revenueByMonth, penaltiesByMonth,
                penaltiesByReason, entries
        );
    }

    private Map<String, BigDecimal> toMap(List<Object[]> rows) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            String key = String.format("%d-%02d", year, month);
            result.put(key, (BigDecimal) row[2]);
        }
        return result;
    }

    private Map<String, BigDecimal> mergeMaps(Map<String, BigDecimal> a, Map<String, BigDecimal> b) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        a.forEach((k, v) -> result.merge(k, v, BigDecimal::add));
        b.forEach((k, v) -> result.merge(k, v, BigDecimal::add));
        return new java.util.TreeMap<>(result);
    }
}
