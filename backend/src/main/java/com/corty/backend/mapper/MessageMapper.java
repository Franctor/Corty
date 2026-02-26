package com.corty.backend.mapper;

import com.corty.backend.dto.MessageResponse;
import com.corty.backend.model.Message;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "message.sender.username", target = "senderUsername")
    @Mapping(source = "message.conversation.idConversation", target = "idConversation")
    @Mapping(source = "message.sender.idUser", target = "idSender")
    @Mapping(target = "isMine", expression = "java(message.getSender().getIdUser().equals(currentUserId))")
    MessageResponse toDTO(Message message, @Context Long currentUserId);

    List<MessageResponse> toDTOList(List<Message> messages, @Context Long currentUserId);
}
