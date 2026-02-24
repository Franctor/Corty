package com.corty.backend.repository;

import com.corty.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdConversationOrderBySentAtAsc(Long conversationId);
}
