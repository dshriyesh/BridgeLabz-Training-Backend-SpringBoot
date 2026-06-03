package com.fundoonotes.service;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ReminderResponse;

import java.util.List;

public interface ReminderService {
    ReminderResponse create(ReminderRequest request);
    ReminderResponse update(Long id, ReminderRequest request);
    ReminderResponse get(Long id);
    List<ReminderResponse> list();
    void delete(Long id);
}
