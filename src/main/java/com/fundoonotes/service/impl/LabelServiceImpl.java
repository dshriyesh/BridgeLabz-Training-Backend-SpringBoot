package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;
import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.LabelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LabelResponse create(LabelRequest request) {
        User user = getCurrentUser();
        Label label = Label.builder().name(request.getName()).user(user).build();
        Label saved = labelRepository.save(label);
        return LabelResponse.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @Override
    @Transactional
    public LabelResponse update(Long id, LabelRequest request) {
        User user = getCurrentUser();
        Label label = labelRepository.findById(id)
                .filter(l -> l.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ValidationException("Label not found for current user"));
        label.setName(request.getName());
        Label saved = labelRepository.save(label);
        return LabelResponse.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        Label label = labelRepository.findById(id)
                .filter(l -> l.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ValidationException("Label not found for current user"));
        var mappedNotes = noteRepository.findByUserAndLabels_Id(user, id);
        for (var note : mappedNotes) {
            note.getLabels().removeIf(existing -> existing.getId().equals(id));
        }
        if (!mappedNotes.isEmpty()) {
            noteRepository.saveAll(mappedNotes);
        }
        labelRepository.delete(label);
        log.info("Deleted label {} for user {}", id, user.getUsername());
    }

    @Override
    public List<LabelResponse> list() {
        User user = getCurrentUser();
        return labelRepository.findByUser(user).stream()
                .map(l -> LabelResponse.builder().id(l.getId()).name(l.getName()).build())
                .toList();
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }
}
