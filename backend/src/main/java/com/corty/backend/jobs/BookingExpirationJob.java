package com.corty.backend.jobs;

import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationJob {

    private static final int EXPIRATION_MINUTES = 15;

    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cancelExpiredPendingPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
        var expired = bookingRepository.findExpiredByStatus(BookingStatus.PENDING_PAYMENT, cutoff);
        if (expired.isEmpty()) return;
        expired.forEach(b -> b.setBookingStatus(BookingStatus.CANCELLED));
        bookingRepository.saveAll(expired);
        log.info("Canceladas {} reservas con pago expirado", expired.size());
    }
}
