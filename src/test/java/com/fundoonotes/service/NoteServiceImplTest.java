package com.fundoonotes.service;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NoteServiceImplTest {

    private NoteServiceImpl noteService;
    private NoteRepository noteRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        noteRepository = Mockito.mock(NoteRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        LabelRepository labelRepository = Mockito.mock(LabelRepository.class);
        noteService = new NoteServiceImpl(noteRepository, userRepository, labelRepository);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice", "", java.util.List.of()));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void archiveShouldUnpinNote() {
        User user = User.builder().id(1L).username("alice").build();
        Note note = Note.builder().id(10L).isPinned(true).isArchived(false).isTrashed(false).user(user).title("n").build();
        Mockito.when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        Mockito.when(noteRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(note));
        Mockito.when(noteRepository.save(Mockito.any(Note.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = noteService.archive(10L);
        assertTrue(response.getIsArchived());
        assertFalse(response.getIsPinned());
    }

    @Test
    void trashShouldSetTrashedAtAndResetPinArchive() {
        User user = User.builder().id(1L).username("alice").build();
        Note note = Note.builder().id(10L).isPinned(true).isArchived(true).isTrashed(false).user(user).title("n").build();
        Mockito.when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        Mockito.when(noteRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(note));
        Mockito.when(noteRepository.save(Mockito.any(Note.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = noteService.trash(10L);
        assertTrue(response.getIsTrashed());
        assertFalse(response.getIsPinned());
        assertFalse(response.getIsArchived());
    }
}
