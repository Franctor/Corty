package com.corty.backend.services;

import com.corty.backend.dto.ClubRequest;
import com.corty.backend.dto.ClubResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.ClubMapper;
import com.corty.backend.model.Booking;
import com.corty.backend.model.City;
import com.corty.backend.model.Club;
import com.corty.backend.model.Court;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.ClubRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.model.Organization;
import com.corty.backend.model.User;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

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
}
