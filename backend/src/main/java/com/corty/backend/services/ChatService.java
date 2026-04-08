package com.corty.backend.services;

import com.corty.backend.dto.MessageRequest;
import com.corty.backend.dto.MessageResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.exception.UnauthorizedActionException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(Message messageDraft, Long currentUserId, Long recipientId) {
        if (currentUserId.equals(recipientId)) {
            throw new BusinessLogicException("No puedes enviarte mensajes a ti mismo");
        }
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado"));

        Conversation conversation = conversationRepository
                .findConversationBetweenTwoUsers(currentUserId, recipientId)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .participants(Set.of(sender, recipient))
                            .build();
                    return conversationRepository.save(newConv);
                });

        LocalDateTime now = LocalDateTime.now();
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(messageDraft.getContent())
                .sentAt(now)
                .read(false)
                .build();
        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        return messageRepository.save(message);
    }

    public List<Conversation> getMyConversations(Long currentUserId) {
        return conversationRepository.findMyConversations(currentUserId);
    }

    @Transactional
    public List<Message> getConversationHistory(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada"));
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(user -> user.getIdUser().equals(currentUserId));
        if (!isParticipant) throw new UnauthorizedActionException("No tienes permiso para ver esta conversación");
        List<Message> history = messageRepository.findByConversationIdConversationOrderBySentAtAsc(conversationId);

        history.stream()
                .filter(m -> !m.getSender().getIdUser().equals(currentUserId) && !m.isRead())
                .forEach(m -> m.setRead(true));

        return history;
    }
}
