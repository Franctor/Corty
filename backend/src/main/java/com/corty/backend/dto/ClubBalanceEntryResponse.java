package com.corty.backend.dto;

import com.corty.backend.model.ClubBalanceEntry;
import com.corty.backend.model.enums.ClubBalanceReason;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClubBalanceEntryResponse {

    private Long id;
    private BigDecimal amount;
    private ClubBalanceReason reason;
    private String description;
    private Long bookingId;
    private LocalDateTime createdAt;

    public static ClubBalanceEntryResponse from(ClubBalanceEntry e) {
        return new ClubBalanceEntryResponse(
                e.getIdEntry(),
                e.getAmount(),
                e.getReason(),
                e.getDescription(),
                e.getBooking() != null ? e.getBooking().getIdBooking() : null,
                e.getCreatedAt()
        );
    }
}
