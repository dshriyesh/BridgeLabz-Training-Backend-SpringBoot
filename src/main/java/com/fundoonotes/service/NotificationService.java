package com.fundoonotes.service;

import com.fundoonotes.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> list();
    NotificationResponse markRead(Long id, boolean read);
    int markAllRead();
    void delete(Long id);
}
