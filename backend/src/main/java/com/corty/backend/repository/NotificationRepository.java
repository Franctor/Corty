package com.corty.backend.repository;

import com.corty.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdUserOrderByCreatedAtDesc(Long userId);

    long countByUserIdUserAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.idUser = :userId AND n.isRead = false")
    void markAllReadByUserId(@Param("userId") Long userId);
}
