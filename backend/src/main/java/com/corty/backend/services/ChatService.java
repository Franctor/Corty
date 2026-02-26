package com.corty.backend.services;

import com.corty.backend.dto.MessageResponse;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.MessageMapper;
import com.corty.backend.model.Conversation;
import com.corty.backend.model.Message;
import com.corty.backend.model.User;
import com.corty.backend.repository.ConversationRepository;
import com.corty.backend.repository.MessageRepository;
import com.corty.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Transactional
    public Message sendMessage(Long senderId, Long recipientId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado"));

        Conversation conversation = conversationRepository
                .findConversationBetweenTwoUsers(senderId, recipientId)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .participants(Set.of(sender, recipient))
                            .build();
                    return conversationRepository.save(newConv);
                });

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return messageRepository.save(message);
    }

    public List<Conversation> getMyConversations(Long userId) {
        return conversationRepository.findMyConversations(userId);
    }

    public List<Message> getConversationHistory(Long conversationId) {
        return messageRepository.findByConversationIdConversationOrderBySentAtAsc(conversationId);
    }
}
