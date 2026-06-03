package com.fundoonotes.repository;

import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Optional<Reminder> findByIdAndUser(Long id, User user);
    Optional<Reminder> findByNoteIdAndUserId(Long noteId, Long userId);
    List<Reminder> findByUserOrderByReminderTimeAsc(User user);
    List<Reminder> findByNotifiedFalseAndReminderTimeLessThanEqual(LocalDateTime time);
}
