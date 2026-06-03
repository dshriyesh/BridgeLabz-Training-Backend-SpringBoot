package com.fundoonotes.repository;

import com.fundoonotes.entity.Attachment;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByIdAndUser(Long id, User user);
    List<Attachment> findByNoteAndUser(Note note, User user);
}
