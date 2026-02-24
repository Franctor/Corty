package com.corty.backend.services;

import com.corty.backend.dto.MessageResponse;
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
    public MessageResponse sendMessage(Long senderId, Long recipientId, String content){
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found")); //Implementar excepciones propias
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        Message savedMessage = messageRepository.save(message);
        return messageMapper.toDTO(savedMessage);
    }

    public List<Conversation> getMyConversations(Long userId) {
        return conversationRepository.findMyConversations(userId);
    }

    public List<MessageResponse> getConversationHistory(Long conversationId) {
        return messageMapper.toDTOList(messageRepository.findByConversationIdConversationOrderBySentAtAsc(conversationId));
    }
}
