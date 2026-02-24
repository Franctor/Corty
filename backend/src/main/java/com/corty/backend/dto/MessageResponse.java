package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long idMessage;
    private String content;
    private LocalDateTime sentAt;
    private Long idSender;
    private String senderUsername;
    private Long idConversation;
}
