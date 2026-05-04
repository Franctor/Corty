package com.corty.backend.repository;

import com.corty.backend.model.Friendship;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    // Buscar si ya existe una relación (para no duplicar)
    Optional<Friendship> findByRequesterAndRecipient(User requester, User recipient);

    // Obtener todas mis amistades aceptadas (da igual si pedí yo o me pidieron)
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user OR f.recipient = :user) AND " +
            "f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("user") User user);

    List<Friendship> findByRecipientAndStatus(User recipient, FriendshipStatus status);
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);

    // Contar solicitudes pendientes para poner el numerito en rojo en el menú
    long countByRecipientAndStatus(User recipient, FriendshipStatus status);
}
