package com.corty.backend.model.enums;

public enum NotificationType {
    // Bookings
    BOOKING_CONFIRMED,  // "Tu reserva en 'Club de Tenis' ha sido confirmada"
    BOOKING_CANCELLED,  // "Lo sentimos, tu pista ha sido cancelada"
    JOIN_REQUEST,       // "Alguien quiere unirse a tu reserva"
    JOIN_ACCEPTED,      // "Tu petición de unirte fue aceptada"
    JOIN_REJECTED,      // "Tu petición de unirte fue rechazada"
    PARTICIPANT_JOINED, // "Un nuevo jugador se ha unido a la reserva"
    PARTICIPANT_LEFT,   // "Un jugador ha abandonado la reserva"
    MATCH_READY,        // "¡Partido completo! Ya sois 4 jugadores"
    RESULT_PENDING,     // "Tu partido ha terminado, registra el resultado"

    // Social
    FRIEND_REQUEST,     // "Alguien quiere ser tu amigo"
    FRIEND_ACCEPTED,    // "Tu solicitud de amistad fue aceptada"
    NEW_MESSAGE,        // "Tienes un mensaje nuevo"

    // Logros
    LEVEL_UP,           // "¡Enhorabuena! Tu nivel de Tenis ha subido a 4.2"

    // System
    SYSTEM_ALERT
}
