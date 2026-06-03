package com.fundoonotes.service.impl;

import com.fundoonotes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoteCleanupScheduler {

    private final NoteRepository noteRepository;

    @Value("${app.notes.trash-retention-days:30}")
    private long trashRetentionDays;

    @Scheduled(fixedDelayString = "${app.notes.cleanup.scheduler-delay-ms:3600000}")
    @Transactional
    public void deleteExpiredTrashedNotes() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(trashRetentionDays);
        var expiredTrashedNotes = noteRepository.findByIsTrashedTrueAndTrashedAtBefore(cutoff);
        if (expiredTrashedNotes.isEmpty()) {
            return;
        }
        int count = expiredTrashedNotes.size();
        noteRepository.deleteAllInBatch(expiredTrashedNotes);
        log.info("Hard-deleted {} trashed notes older than {} days", count, trashRetentionDays);
    }
}
