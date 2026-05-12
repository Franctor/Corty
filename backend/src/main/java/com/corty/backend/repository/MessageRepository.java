package com.corty.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdConversationOrderBySentAtAsc(Long conversationId);

    @Modifying
    @Query("UPDATE Message m SET m.sender = null WHERE m.sender.idUser = :userId")
    void detachSender(@Param("userId") Long userId);
}
