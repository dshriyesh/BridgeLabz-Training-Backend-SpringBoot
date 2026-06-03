package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteService;
import com.fundoonotes.util.NoteSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    @Override
    @Transactional
    public NoteResponse create(NoteRequest request) {
        User user = getCurrentUser();
        Note note = Note.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .color(request.getColor())
                .isPinned(false)
                .isArchived(false)
                .isTrashed(false)
                .trashedAt(null)
                .user(user)
                .labels(resolveLabels(request, user))
                .build();
        Note saved = noteRepository.save(note);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public NoteResponse update(Long id, NoteRequest request) {
        User user = getCurrentUser();
        Note note = getOwnedNote(id, user);
        note.setTitle(request.getTitle());
        note.setDescription(request.getDescription());
        note.setColor(request.getColor());
        note.setLabels(resolveLabels(request, user));
        return toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        Note note = getOwnedNote(id, user);
        noteRepository.delete(note);
        log.info("Note deleted permanently: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse get(Long id) {
        User user = getCurrentUser();
        return toResponse(getOwnedNote(id, user));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> list(Pageable pageable) {
        User user = getCurrentUser();
        return noteRepository.findByUser(user, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> search(String title, String description, String color, Boolean pinned, Boolean archived, Boolean trashed, Long labelId, LocalDate fromDate, LocalDate toDate, LocalDateTime reminderFrom, LocalDateTime reminderTo, Pageable pageable) {
        User user = getCurrentUser();
        return noteRepository.findAll(NoteSpecification.byFilters(user, title, description, color, pinned, archived, trashed, labelId, fromDate, toDate, reminderFrom, reminderTo), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public NoteResponse pin(Long id) {
        User user = getCurrentUser();
        Note note = getOwnedNote(id, user);
        if (Boolean.TRUE.equals(note.getIsArchived())) {
            note.setIsArchived(false);
        }
        note.setIsPinned(!Boolean.TRUE.equals(note.getIsPinned()));
        if (Boolean.TRUE.equals(note.getIsPinned()) && Boolean.TRUE.equals(note.getIsArchived())) {
            throw new ValidationException("Pinned and archived cannot both be true");
        }
        return toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteResponse archive(Long id) {
        User user = getCurrentUser();
        Note note = getOwnedNote(id, user);
        if (Boolean.TRUE.equals(note.getIsPinned())) {
            note.setIsPinned(false);
        }
        note.setIsArchived(!Boolean.TRUE.equals(note.getIsArchived()));
        if (Boolean.TRUE.equals(note.getIsPinned()) && Boolean.TRUE.equals(note.getIsArchived())) {
            throw new ValidationException("Pinned and archived cannot both be true");
        }
        return toResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteResponse trash(Long id) {
        User user = getCurrentUser();
        Note note = getOwnedNote(id, user);
        boolean nextValue = !Boolean.TRUE.equals(note.getIsTrashed());
        note.setIsTrashed(nextValue);
        note.setTrashedAt(nextValue ? LocalDateTime.now() : null);
        if (nextValue) {
            note.setIsPinned(false);
            note.setIsArchived(false);
        }
        return toResponse(noteRepository.save(note));
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    private Note getOwnedNote(Long id, User user) {
        return noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
    }

    private Set<Label> resolveLabels(NoteRequest request, User user) {
        Set<Long> ids = new HashSet<>();
        if (request.getLabelId() != null) {
            ids.add(request.getLabelId());
        }
        if (request.getLabelIds() != null) {
            ids.addAll(request.getLabelIds());
        }
        if (ids.isEmpty()) {
            return new HashSet<>();
        }

        Set<Label> labels = new HashSet<>();
        for (Long id : ids) {
            Label label = labelRepository.findById(id)
                    .filter(found -> found.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new ValidationException("Label not found for current user: " + id));
            labels.add(label);
        }
        return labels;
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .color(note.getColor())
                .isPinned(note.getIsPinned())
                .isArchived(note.getIsArchived())
                .isTrashed(note.getIsTrashed())
                .reminderTime(note.getReminderTime())
                .labels((note.getLabels() == null ? Collections.<Label>emptySet() : note.getLabels()).stream().map(Label::getName).toList())
                .build();
    }
}
