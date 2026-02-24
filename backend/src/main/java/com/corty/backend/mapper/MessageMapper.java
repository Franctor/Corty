package com.corty.backend.mapper;

import com.corty.backend.dto.MessageResponse;
import com.corty.backend.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(source = "sender.username", target = "senderUsername")
    @Mapping(source = "conversation.idConversation", target = "idConversation")
    @Mapping(source = "sender.idUser", target = "idSender")
    MessageResponse toDTO(Message message);

    List<MessageResponse> toDTOList(List<Message> messages);
}
