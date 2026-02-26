package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {
    private Long idConversation;
    private LocalDateTime lastMessageAt;

    // Información del "otro" participante para mostrar en la lista
    private Long otherParticipantId;
    private String otherParticipantName;
    private String otherParticipantAvatar;

    // Previsualización del último mensaje
    private String lastMessageContent;
    private boolean lastMessageRead;

    // Contador de mensajes no leídos para la burbuja de notificación
    private long unreadMessagesCount;
}
