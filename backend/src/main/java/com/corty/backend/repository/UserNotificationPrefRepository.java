package com.corty.backend.repository;

import com.corty.backend.model.UserNotificationPref;
import com.corty.backend.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationPrefRepository extends JpaRepository<UserNotificationPref, Long> {

    List<UserNotificationPref> findByUserIdUser(Long userId);

    Optional<UserNotificationPref> findByUserIdUserAndNotificationType(Long userId, NotificationType type);
}
