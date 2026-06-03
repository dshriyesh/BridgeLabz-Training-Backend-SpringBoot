package com.fundoonotes.repository;

import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByUser(User user);
}
