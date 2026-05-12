package com.corty.backend.dto;

import com.corty.backend.model.enums.JoinRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class PublicBookingResponse {

    private Long id;
    private String courtName;
    private String clubName;
    private String clubLogoUrl;
    private java.math.BigDecimal clubLat;
    private java.math.BigDecimal clubLng;
    private String sport;
    private String sportIconUrl;
    private String sportColor;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int currentPlayers;
    private int maxPlayers;
    private double avgLevel;
    private double totalPrice;
    private double pricePerPlayer;
    private boolean splitPayment;
    private String notes;
    private Double distanceKm;
    // Estado de la petición del usuario actual (null = no ha pedido)
    private JoinRequestStatus myRequestStatus;
    private int ownerKarma;
}
