package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.EmailNotificationEvent;
import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ReminderResponse;
import com.fundoonotes.entity.InAppNotification;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.EmailEventProducer;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.InAppNotificationRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final InAppNotificationRepository inAppNotificationRepository;
    private final EmailEventProducer emailEventProducer;

    @Override
    @Transactional
    public ReminderResponse create(ReminderRequest request) {
        User user = getCurrentUser();
        Note note = noteRepository.findByIdAndUser(request.getNoteId(), user)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + request.getNoteId()));
        validateReminderTime(request.getReminderTime());

        Reminder reminder = reminderRepository.findByNoteIdAndUserId(note.getId(), user.getId())
                .orElse(Reminder.builder().note(note).user(user).build());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setNotified(false);
        note.setReminderTime(request.getReminderTime());
        Reminder saved = reminderRepository.save(reminder);
        noteRepository.save(note);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReminderResponse update(Long id, ReminderRequest request) {
        User user = getCurrentUser();
        Reminder reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("Reminder not found for current user"));
        if (!reminder.getNote().getId().equals(request.getNoteId())) {
            throw new ValidationException("Reminder noteId does not match existing reminder note");
        }
        validateReminderTime(request.getReminderTime());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setNotified(false);
        Note note = reminder.getNote();
        note.setReminderTime(request.getReminderTime());
        noteRepository.save(note);
        return toResponse(reminderRepository.save(reminder));
    }

    @Override
    public ReminderResponse get(Long id) {
        User user = getCurrentUser();
        Reminder reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("Reminder not found for current user"));
        return toResponse(reminder);
    }

    @Override
    public List<ReminderResponse> list() {
        User user = getCurrentUser();
        return reminderRepository.findByUserOrderByReminderTimeAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        Reminder reminder = reminderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ValidationException("Reminder not found for current user"));
        Note note = reminder.getNote();
        note.setReminderTime(null);
        noteRepository.save(note);
        reminderRepository.delete(reminder);
    }

    @Scheduled(fixedDelayString = "${app.reminder.scheduler-delay-ms:60000}")
    @Transactional
    public void processPendingReminders() {
        LocalDateTime now = LocalDateTime.now(IST);
        log.info("Checking reminders at IST: {}", now);
        var due = reminderRepository.findByNotifiedFalseAndReminderTimeLessThanEqual(now);
        for (Reminder reminder : due) {
            String message = "Reminder: " + reminder.getNote().getTitle();
            inAppNotificationRepository.save(InAppNotification.builder()
                    .user(reminder.getUser())
                    .message(message)
                    .isRead(false)
                    .build());
            emailEventProducer.publish(EmailNotificationEvent.builder()
                    .to(reminder.getUser().getEmail())
                    .subject("Fundoo Reminder")
                    .body(message)
                    .build());
            reminder.setNotified(true);
            reminderRepository.save(reminder);
            log.info("Processed reminder {} for user {}", reminder.getId(), reminder.getUser().getUsername());
        }
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    private ReminderResponse toResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .noteId(reminder.getNote().getId())
                .reminderTime(reminder.getReminderTime())
                .notified(reminder.getNotified())
                .build();
    }

    private void validateReminderTime(LocalDateTime reminderTime) {
        if (reminderTime.isBefore(LocalDateTime.now(IST))) {
            throw new ValidationException("Reminder time cannot be in the past");
        }
    }
}



// package com.fundoonotes.service.impl;

// import com.fundoonotes.dto.request.EmailNotificationEvent;
// import com.fundoonotes.dto.request.ReminderRequest;
// import com.fundoonotes.dto.response.ReminderResponse;
// import com.fundoonotes.entity.InAppNotification;
// import com.fundoonotes.entity.Note;
// import com.fundoonotes.entity.Reminder;
// import com.fundoonotes.entity.User;
// import com.fundoonotes.event.EmailEventProducer;
// import com.fundoonotes.exception.NoteNotFoundException;
// import com.fundoonotes.exception.UserNotFoundException;
// import com.fundoonotes.exception.ValidationException;
// import com.fundoonotes.repository.InAppNotificationRepository;
// import com.fundoonotes.repository.NoteRepository;
// import com.fundoonotes.repository.ReminderRepository;
// import com.fundoonotes.repository.UserRepository;
// import com.fundoonotes.service.ReminderService;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.List;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class ReminderServiceImpl implements ReminderService {

//     private final ReminderRepository reminderRepository;
//     private final NoteRepository noteRepository;
//     private final UserRepository userRepository;
//     private final InAppNotificationRepository inAppNotificationRepository;
//     private final EmailEventProducer emailEventProducer;

//     @Override
//     @Transactional
//     public ReminderResponse create(ReminderRequest request) {
//         User user = getCurrentUser();
//         Note note = noteRepository.findByIdAndUser(request.getNoteId(), user)
//                 .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + request.getNoteId()));
//         validateReminderTime(request.getReminderTime());

//         Reminder reminder = reminderRepository.findByNoteIdAndUserId(note.getId(), user.getId())
//                 .orElse(Reminder.builder().note(note).user(user).build());
//         reminder.setReminderTime(request.getReminderTime());
//         reminder.setNotified(false);
//         note.setReminderTime(request.getReminderTime());
//         Reminder saved = reminderRepository.save(reminder);
//         noteRepository.save(note);

//         return toResponse(saved);
//     }

//     @Override
//     @Transactional
//     public ReminderResponse update(Long id, ReminderRequest request) {
//         User user = getCurrentUser();
//         Reminder reminder = reminderRepository.findByIdAndUser(id, user)
//                 .orElseThrow(() -> new ValidationException("Reminder not found for current user"));
//         if (!reminder.getNote().getId().equals(request.getNoteId())) {
//             throw new ValidationException("Reminder noteId does not match existing reminder note");
//         }
//         validateReminderTime(request.getReminderTime());
//         reminder.setReminderTime(request.getReminderTime());
//         reminder.setNotified(false);
//         Note note = reminder.getNote();
//         note.setReminderTime(request.getReminderTime());
//         noteRepository.save(note);
//         return toResponse(reminderRepository.save(reminder));
//     }

//     @Override
//     public ReminderResponse get(Long id) {
//         User user = getCurrentUser();
//         Reminder reminder = reminderRepository.findByIdAndUser(id, user)
//                 .orElseThrow(() -> new ValidationException("Reminder not found for current user"));
//         return toResponse(reminder);
//     }

//     @Override
//     public List<ReminderResponse> list() {
//         User user = getCurrentUser();
//         return reminderRepository.findByUserOrderByReminderTimeAsc(user).stream()
//                 .map(this::toResponse)
//                 .toList();
//     }

//     @Override
//     @Transactional
//     public void delete(Long id) {
//         User user = getCurrentUser();
//         Reminder reminder = reminderRepository.findByIdAndUser(id, user)
//                 .orElseThrow(() -> new ValidationException("Reminder not found for current user"));
//         Note note = reminder.getNote();
//         note.setReminderTime(null);
//         noteRepository.save(note);
//         reminderRepository.delete(reminder);
//     }

//     @Scheduled(fixedDelayString = "${app.reminder.scheduler-delay-ms:60000}")
//     @Transactional
//     public void processPendingReminders() {
//         var due = reminderRepository.findByNotifiedFalseAndReminderTimeLessThanEqual(LocalDateTime.now());
//         for (Reminder reminder : due) {
//             String message = "Reminder: " + reminder.getNote().getTitle();
//             inAppNotificationRepository.save(InAppNotification.builder()
//                     .user(reminder.getUser())
//                     .message(message)
//                     .isRead(false)
//                     .build());
//             emailEventProducer.publish(EmailNotificationEvent.builder()
//                     .to(reminder.getUser().getEmail())
//                     .subject("Fundoo Reminder")
//                     .body(message)
//                     .build());
//             reminder.setNotified(true);
//             reminderRepository.save(reminder);
//             log.info("Processed reminder {} for user {}", reminder.getId(), reminder.getUser().getUsername());
//         }
//     }

//     private User getCurrentUser() {
//         String principal = SecurityContextHolder.getContext().getAuthentication().getName();
//         return userRepository.findByUsername(principal)
//                 .or(() -> userRepository.findByEmail(principal))
//                 .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
//     }

//     private ReminderResponse toResponse(Reminder reminder) {
//         return ReminderResponse.builder()
//                 .id(reminder.getId())
//                 .noteId(reminder.getNote().getId())
//                 .reminderTime(reminder.getReminderTime())
//                 .notified(reminder.getNotified())
//                 .build();
//     }

//     private void validateReminderTime(LocalDateTime reminderTime) {
//         if (reminderTime.isBefore(LocalDateTime.now())) {
//             throw new ValidationException("Reminder time cannot be in the past");
//         }
//     }
// }
