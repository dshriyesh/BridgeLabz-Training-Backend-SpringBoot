package com.fundoonotes.service.impl;

import com.fundoonotes.dto.response.NotificationResponse;
import com.fundoonotes.entity.InAppNotification;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.InAppNotificationRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final InAppNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<NotificationResponse> list() {
        User user = getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedDateDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long id, boolean read) {
        User user = getCurrentUser();
        InAppNotification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("Notification not found for current user"));
        notification.setIsRead(read);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllRead() {
        User user = getCurrentUser();
        List<InAppNotification> notifications = notificationRepository.findByUserOrderByCreatedDateDesc(user);
        int updatedCount = 0;
        for (InAppNotification notification : notifications) {
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(true);
                updatedCount++;
            }
        }
        if (updatedCount > 0) {
            notificationRepository.saveAll(notifications);
        }
        return updatedCount;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        InAppNotification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("Notification not found for current user"));
        notificationRepository.delete(notification);
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    private NotificationResponse toResponse(InAppNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdDate(notification.getCreatedDate())
                .build();
    }
}
