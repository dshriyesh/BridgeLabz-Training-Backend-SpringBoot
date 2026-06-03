package com.fundoonotes.repository;

import com.fundoonotes.entity.InAppNotification;
import com.fundoonotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    List<InAppNotification> findByUserOrderByCreatedDateDesc(User user);
    Optional<InAppNotification> findByIdAndUser(Long id, User user);
}
