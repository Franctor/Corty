package com.corty.backend.repository;

import com.corty.backend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    // Busca conversaciones donde el usuario es uno de los participantes
    // y las ordena para que las que tienen mensajes nuevos salgan arriba
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findMyConversations(@Param("userId") Long userId);

    // Busca si existe una conversación exacta entre dos personas
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE p1.id = :id1 AND p2.id = :id2")
    Optional<Conversation> findConversationBetweenTwoUsers(@Param("id1") Long id1, @Param("id2") Long id2);
}
