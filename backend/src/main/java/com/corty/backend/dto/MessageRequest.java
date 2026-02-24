package com.corty.backend.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long senderId;
    private Long recipientId;
    private String content;
}
