package com.corty.backend.model.enums;

public enum NotificationType {
    // Bookings
    INVITATION,        // "Te han invitado a un partido de Pádel"
    BOOKING_CONFIRMED, // "Tu reserva en 'Club de Tenis' ha sido confirmada"
    BOOKING_CANCELLED, // "Lo sentimos, tu pista ha sido cancelada por lluvia"

    // Payments
    PAYMENT_SUCCESS,   // "Pago de 5.00€ realizado correctamente"
    PAYMENT_PENDING,   // "Recuerda pagar tu parte del partido de las 18:00"

    // Social
    MATCH_READY,       // "¡Partido completo! Ya sois 4 jugadores"
    NEW_REVIEW,        // "Alguien ha valorado tu juego en el último partido"
    LEVEL_UP,          // "¡Enhorabuena! Tu nivel de Tenis ha subido a 4.2"

    // System
    SYSTEM_ALERT
}
