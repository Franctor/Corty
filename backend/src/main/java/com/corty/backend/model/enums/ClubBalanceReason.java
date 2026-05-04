package com.corty.backend.model.enums;

public enum ClubBalanceReason {
    /** Jugador cancela entre 2h y 24h antes — club retiene 50% */
    PARTICIPANT_LATE_CANCEL,
    /** Jugador cancela con menos de 2h — club retiene 100% */
    PARTICIPANT_LAST_MINUTE_CANCEL,
    /** Owner cancela entre 2h y 24h antes — club retiene 50% */
    OWNER_LATE_CANCEL,
    /** Owner cancela con menos de 2h — club retiene 100% */
    OWNER_LAST_MINUTE_CANCEL,
}
