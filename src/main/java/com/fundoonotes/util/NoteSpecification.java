package com.fundoonotes.util;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class NoteSpecification {
    private NoteSpecification() {
    }

    public static Specification<Note> byFilters(User user, String title, String description, String color, Boolean pinned, Boolean archived, Boolean trashed, Long labelId, LocalDate fromDate, LocalDate toDate, LocalDateTime reminderFrom, LocalDateTime reminderTo) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            predicate = cb.and(predicate, cb.equal(root.get("user"), user));

            if (title != null && !title.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (description != null && !description.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }
            if (color != null && !color.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("color")), color.toLowerCase()));
            }
            if (pinned != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isPinned"), pinned));
            }
            if (archived != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isArchived"), archived));
            }
            if (trashed != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isTrashed"), trashed));
            }
            if (labelId != null) {
                query.distinct(true);
                predicate = cb.and(predicate, cb.equal(root.join("labels").get("id"), labelId));
            }
            if (fromDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdDate"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                LocalDateTime end = toDate.plusDays(1).atStartOfDay();
                predicate = cb.and(predicate, cb.lessThan(root.get("createdDate"), end));
            }
            if (reminderFrom != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("reminderTime"), reminderFrom));
            }
            if (reminderTo != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("reminderTime"), reminderTo));
            }

            return predicate;
        };
    }
}
