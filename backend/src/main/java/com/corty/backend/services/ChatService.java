package com.corty.backend.services;

import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.exception.UnauthorizedActionException;
import com.corty.backend.model.Conversation;
import com.corty.backend.model.Message;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.repository.ConversationRepository;
import com.corty.backend.repository.MessageRepository;
import com.corty.backend.repository.PlayerRepository;
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
    private final NotificationService notificationService;
    private final PlayerRepository playerRepository;

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

        Message saved = messageRepository.save(message);

        String senderName = playerRepository.findByUser_IdUser(currentUserId)
                .map(p -> p.getName() + " " + p.getSurname())
                .orElse(sender.getUsername());
        notificationService.send(
                recipientId,
                NotificationType.NEW_MESSAGE,
                "Nuevo mensaje de " + senderName,
                messageDraft.getContent().length() > 60
                        ? messageDraft.getContent().substring(0, 60) + "…"
                        : messageDraft.getContent(),
                conversation.getIdConversation()
        );

        return saved;
    }

    public List<Conversation> getMyConversations(Long currentUserId) {
        return conversationRepository.findMyConversations(currentUserId);
    }

    public java.util.Optional<Conversation> findConversationWith(Long currentUserId, Long recipientId) {
        return conversationRepository.findConversationBetweenTwoUsers(currentUserId, recipientId);
    }

    @Transactional
    public void markConversationRead(Long conversationId, Long currentUserId) {
        messageRepository.findByConversationIdConversationOrderBySentAtAsc(conversationId).stream()
                .filter(m -> !m.getSender().getIdUser().equals(currentUserId) && !m.isRead())
                .forEach(m -> m.setRead(true));
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
