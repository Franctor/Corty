package com.corty.backend.dto;

import com.corty.backend.model.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private NotificationType type;
    private Long referenceId;
}
