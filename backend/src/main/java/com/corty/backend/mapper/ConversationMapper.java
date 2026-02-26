package com.corty.backend.mapper;

import com.corty.backend.dto.ConversationResponse;
import com.corty.backend.model.Conversation;
import com.corty.backend.model.Message;
import com.corty.backend.model.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "otherParticipantId", expression = "java(getOtherUser(conversation, currentUserId).getIdUser())")
    @Mapping(target = "otherParticipantName", expression = "java(getOtherUser(conversation, currentUserId).getUsername())")
    @Mapping(target = "otherParticipantAvatar", expression = "java(getOtherParticipantAvatar(conversation, currentUserId))")
    @Mapping(target = "lastMessageContent", source = "conversation", qualifiedByName = "extractLastMessageContent")
    @Mapping(target = "lastMessageRead", source = "conversation", qualifiedByName = "extractLastMessageRead")
    @Mapping(target = "unreadMessagesCount", expression = "java(countUnread(conversation, currentUserId))")
    ConversationResponse toResponse(Conversation conversation, @Context Long currentUserId);

    List<ConversationResponse> toResponseList(List<Conversation> conversations, @Context Long currentUserId);

    // Lógica para obtener el otro usuario (el que no es el que consulta)
    default User getOtherUser(Conversation conversation, Long currentUserId) {
        return conversation.getParticipants().stream()
                .filter(u -> !u.getIdUser().equals(currentUserId))
                .findFirst()
                .orElse(new User()); // O manejar error
    }

    default String getOtherParticipantAvatar(Conversation conversation, Long currentUserId) {
        User other = getOtherUser(conversation, currentUserId);
        return (other.getPlayer() != null) ? other.getPlayer().getAvatarUrl() : null;
    }

    @Named("extractLastMessageContent")
    default String extractLastMessageContent(Conversation conversation) {
        return conversation.getMessages().stream()
                .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                .map(Message::getContent)
                .findFirst()
                .orElse("Sin mensajes todavía");
    }

    @Named("extractLastMessageRead")
    default boolean extractLastMessageRead(Conversation conversation) {
        return conversation.getMessages().stream()
                .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                .map(Message::isRead)
                .findFirst()
                .orElse(true);
    }

    default long countUnread(Conversation conversation, Long currentUserId) {
        return conversation.getMessages().stream()
                .filter(m -> !m.isRead() && !m.getSender().getIdUser().equals(currentUserId))
                .count();
    }
}