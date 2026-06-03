package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    @EntityGraph(attributePaths = "labels")
    Page<Note> findByUser(User user, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "labels")
    Page<Note> findAll(Specification<Note> spec, Pageable pageable);

    @EntityGraph(attributePaths = "labels")
    java.util.Optional<Note> findByIdAndUser(Long id, User user);
    List<Note> findByUserAndLabels_Id(User user, Long labelId);
    List<Note> findByIsTrashedTrueAndTrashedAtBefore(LocalDateTime cutoff);
}
