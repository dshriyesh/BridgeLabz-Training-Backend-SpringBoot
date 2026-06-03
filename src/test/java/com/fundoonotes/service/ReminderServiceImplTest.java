package com.fundoonotes.service;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.EmailEventProducer;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.InAppNotificationRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.impl.ReminderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReminderServiceImplTest {

    private ReminderServiceImpl reminderService;
    private ReminderRepository reminderRepository;
    private NoteRepository noteRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        reminderRepository = Mockito.mock(ReminderRepository.class);
        noteRepository = Mockito.mock(NoteRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        InAppNotificationRepository inAppNotificationRepository = Mockito.mock(InAppNotificationRepository.class);
        EmailEventProducer emailEventProducer = Mockito.mock(EmailEventProducer.class);
        reminderService = new ReminderServiceImpl(reminderRepository, noteRepository, userRepository, inAppNotificationRepository, emailEventProducer);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice", "", java.util.List.of()));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldRejectPastReminderTime() {
        User user = User.builder().id(1L).username("alice").build();
        Note note = Note.builder().id(2L).title("n").user(user).build();
        ReminderRequest req = new ReminderRequest();
        req.setNoteId(2L);
        req.setReminderTime(LocalDateTime.now().minusMinutes(5));

        Mockito.when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        Mockito.when(noteRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(note));

        assertThrows(ValidationException.class, () -> reminderService.create(req));
    }

    @Test
    void createShouldPassForFutureReminder() {
        User user = User.builder().id(1L).username("alice").build();
        Note note = Note.builder().id(2L).title("n").user(user).build();
        ReminderRequest req = new ReminderRequest();
        req.setNoteId(2L);
        req.setReminderTime(LocalDateTime.now().plusMinutes(10));

        Mockito.when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        Mockito.when(noteRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(note));
        Mockito.when(reminderRepository.findByNoteIdAndUserId(2L, 1L)).thenReturn(Optional.empty());
        Mockito.when(reminderRepository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);
        Mockito.when(noteRepository.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        assertDoesNotThrow(() -> reminderService.create(req));
    }
}
