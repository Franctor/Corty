package com.corty.backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.corty.backend.dto.BookingAdminDetailResponse;
import com.corty.backend.dto.BookingAdminResponse;
import com.corty.backend.dto.BookingAdminUpdateRequest;
import com.corty.backend.dto.BookingPresencialRequest;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Court;
import com.corty.backend.model.Organization;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.BookingType;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.OrganizationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingAdminService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailService emailService;

    public Page<BookingAdminResponse> getAll(int page, int size, String search, User principal) {
        PageRequest pageable = PageRequest.of(page, size);
        boolean isOrg = principal.getRole() != null && "ORGANIZATION".equals(principal.getRole().getName());
        if (isOrg) {
            Organization org = organizationRepository.findByUser_IdUser(principal.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada"));
            return bookingRepository.findAllFilteredByOrg(org.getIdOrganization(), search, pageable)
                    .map(this::toSummaryResponse);
        }
        return bookingRepository.findAllFiltered(search, pageable)
                .map(this::toSummaryResponse);
    }

    @Transactional
    public BookingAdminDetailResponse createPresencial(BookingPresencialRequest request, User admin) {
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Pista no encontrada"));

        if (bookingRepository.existsOverlappingBooking(
                court.getIdCourt(), request.getDate(), request.getStartTime(), request.getEndTime())) {
            throw new BusinessLogicException("El horario seleccionado ya está ocupado");
        }

        long minutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        BigDecimal totalPrice = court.getPricePerHour()
                .multiply(BigDecimal.valueOf(minutes))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        Booking booking = Booking.builder()
                .court(court)
                .owner(admin)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bookingType(BookingType.PRIVATE)
                .bookingStatus(BookingStatus.CONFIRMED)
                .paymentMethod(request.getPaymentMethod())
                .splitPayment(false)
                .fullyPaid(true)
                .notes(request.getNotes())
                .totalPrice(totalPrice)
                .build();

        return toDetailResponse(bookingRepository.save(booking));
    }

    public BookingAdminDetailResponse getById(Long id) {
        return toDetailResponse(findOrThrow(id));
    }

    @Transactional
    public BookingAdminDetailResponse update(Long id, BookingAdminUpdateRequest request) {
        Booking booking = findOrThrow(id);

        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes().isBlank() ? null : request.getNotes().trim());
        }

        if (request.getBookingStatus() != null) {
            BookingStatus newStatus = BookingStatus.valueOf(request.getBookingStatus());
            boolean isCancelling = newStatus == BookingStatus.CANCELLED
                    && booking.getBookingStatus() != BookingStatus.CANCELLED;
            booking.setBookingStatus(newStatus);
            bookingRepository.save(booking);
            if (isCancelling) {
                notifyParticipantsCancelled(booking, request.getCancelReason());
            }
        } else {
            bookingRepository.save(booking);
        }

        return toDetailResponse(booking);
    }

    private void notifyParticipantsCancelled(Booking booking, String reason) {
        String date = booking.getDate().toString();
        String startTime = booking.getStartTime().toString();
        String courtName = booking.getCourt().getName();
        String clubName = booking.getCourt().getClub().getName();

        for (PlayerBooking participation : booking.getParticipants()) {
            String email = participation.getPlayer().getUser().getEmail();
            String username = participation.getPlayer().getUser().getUsername();
            emailService.sendBookingCancelled(email, username, courtName, clubName, date, startTime, reason);
        }

        String ownerEmail = booking.getOwner().getEmail();
        String ownerUsername = booking.getOwner().getUsername();
        emailService.sendBookingCancelled(ownerEmail, ownerUsername, courtName, clubName, date, startTime, reason);
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    private BookingAdminResponse toSummaryResponse(Booking booking) {
        return BookingAdminResponse.builder()
                .id(booking.getIdBooking())
                .date(booking.getDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .bookingType(booking.getBookingType().name())
                .bookingStatus(booking.getBookingStatus().name())
                .totalPrice(booking.getTotalPrice())
                .fullyPaid(booking.isFullyPaid())
                .splitPayment(booking.isSplitPayment())
                .paymentMethod(booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : null)
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .courtName(booking.getCourt().getName())
                .clubName(booking.getCourt().getClub().getName())
                .ownerUsername(booking.getOwner().getUsername())
                .participantCount(booking.getParticipants().size())
                .build();
    }

    private BookingAdminDetailResponse toDetailResponse(Booking booking) {
        List<BookingAdminDetailResponse.ParticipantRow> participantRows = booking.getParticipants().stream()
                .map(participation -> BookingAdminDetailResponse.ParticipantRow.builder()
                .username(participation.getPlayer().getUser().getUsername())
                .fullName(participation.getPlayer().getName() + " " + participation.getPlayer().getSurname())
                .splitPrice(participation.getSplitPrice())
                .hasPaid(participation.isHasPaid())
                .confirmed(participation.isConfirmed())
                .winner(participation.isWinner())
                .owner(participation.getPlayer().getUser().getIdUser()
                        .equals(booking.getOwner().getIdUser()))
                .build())
                .collect(Collectors.toList());

        return BookingAdminDetailResponse.builder()
                .id(booking.getIdBooking())
                .date(booking.getDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .bookingType(booking.getBookingType().name())
                .bookingStatus(booking.getBookingStatus().name())
                .totalPrice(booking.getTotalPrice())
                .fullyPaid(booking.isFullyPaid())
                .splitPayment(booking.isSplitPayment())
                .paymentMethod(booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : null)
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .courtName(booking.getCourt().getName())
                .clubName(booking.getCourt().getClub().getName())
                .ownerUsername(booking.getOwner().getUsername())
                .participants(participantRows)
                .build();
    }
}
