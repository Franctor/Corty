package com.corty.backend.controller;

import com.corty.backend.dto.MessageRequest;
import com.corty.backend.dto.MessageResponse;
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

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
        MessageResponse message = chatService.sendMessage(
                request.getSenderId(),
                request.getRecipientId(),
                request.getContent()
        );
        return ResponseEntity.ok(message);
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<Conversation>> getMyConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getMyConversations(userId));
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getHistory(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getConversationHistory(conversationId));
    }
}
