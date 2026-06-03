package com.fundoonotes.service;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface NoteService {
    NoteResponse create(NoteRequest request);
    NoteResponse update(Long id, NoteRequest request);
    void delete(Long id);
    NoteResponse get(Long id);
    Page<NoteResponse> list(Pageable pageable);
    Page<NoteResponse> search(String title, String description, String color, Boolean pinned, Boolean archived, Boolean trashed, Long labelId, LocalDate fromDate, LocalDate toDate, LocalDateTime reminderFrom, LocalDateTime reminderTo, Pageable pageable);
    NoteResponse pin(Long id);
    NoteResponse archive(Long id);
    NoteResponse trash(Long id);
}
