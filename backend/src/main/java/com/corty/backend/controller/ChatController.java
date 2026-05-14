package com.corty.backend.controller;

import com.corty.backend.dto.ConversationResponse;
import com.corty.backend.dto.MessageRequest;
import com.corty.backend.dto.MessageResponse;
import com.corty.backend.mapper.ConversationMapper;
import com.corty.backend.mapper.MessageMapper;
import com.corty.backend.model.Conversation;
import com.corty.backend.model.Message;
import com.corty.backend.model.User;
import com.corty.backend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Social")
@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request, @AuthenticationPrincipal User currentUser) {
        Message messageDraft = messageMapper.toEntity(request);
        Message savedMessage = chatService.sendMessage(messageDraft, currentUser.getIdUser(), request.getRecipientId());
        return ResponseEntity.status(201).body(messageMapper.toDTO(savedMessage, request.getSenderId()));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getMyConversations(@AuthenticationPrincipal User currentUser) {
        List<Conversation> conversations = chatService.getMyConversations(currentUser.getIdUser());
        return ResponseEntity.ok(conversationMapper.toDtoList(conversations, currentUser.getIdUser()));
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getHistory(@PathVariable Long conversationId, @AuthenticationPrincipal User currentUser) {
        List<Message> history = chatService.getConversationHistory(conversationId, currentUser.getIdUser());
        return ResponseEntity.ok(messageMapper.toDTOList(history, currentUser.getIdUser()));
    }

    @PatchMapping("/history/{conversationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long conversationId, @AuthenticationPrincipal User currentUser) {
        chatService.markConversationRead(conversationId, currentUser.getIdUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with/{recipientId}")
    public ResponseEntity<ConversationResponse> getConversationWith(
            @PathVariable Long recipientId,
            @AuthenticationPrincipal User currentUser) {
        return chatService.findConversationWith(currentUser.getIdUser(), recipientId)
                .map(c -> ResponseEntity.ok(conversationMapper.toDto(c, currentUser.getIdUser())))
                .orElse(ResponseEntity.noContent().build());
    }
}
