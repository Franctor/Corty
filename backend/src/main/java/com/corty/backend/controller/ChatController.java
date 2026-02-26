package com.corty.backend.controller;

import com.corty.backend.dto.ConversationResponse;
import com.corty.backend.dto.MessageRequest;
import com.corty.backend.dto.MessageResponse;
import com.corty.backend.mapper.ConversationMapper;
import com.corty.backend.mapper.MessageMapper;
import com.corty.backend.model.Conversation;
import com.corty.backend.model.Message;
import com.corty.backend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        Message savedMessage = chatService.sendMessage(
                request.getSenderId(),
                request.getRecipientId(),
                request.getContent()
        );
        return ResponseEntity.ok(messageMapper.toDTO(savedMessage, request.getSenderId()));
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationResponse>> getMyConversations(@PathVariable Long userId) {
        //IMPLEMENT DTO HERE
        List<Conversation> conversations = chatService.getMyConversations(userId);
        return ResponseEntity.ok(conversationMapper.toResponseList(conversations, userId));
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getHistory(@PathVariable Long conversationId) {
        List<Message> history = chatService.getConversationHistory(conversationId);
        return ResponseEntity.ok(messageMapper.toDTOList(history));
    }
}
