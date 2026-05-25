package com.tepswords.back.repository;

import com.tepswords.back.model.WordStudyEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WordStudyEventRepository extends JpaRepository<WordStudyEvent, Long> {
    List<WordStudyEvent> findByStudiedAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);
}
