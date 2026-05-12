package com.corty.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

    private Long idMessage;
    private String content;
    private LocalDateTime sentAt;
    private Long idSender;
    private String senderUsername;
    private Long idConversation;
    private boolean read;
    private boolean isMine;
}
