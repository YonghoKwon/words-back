package com.tepswords.back.repository;

import com.tepswords.back.model.WordStudyHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WordStudyHistoryRepository extends JpaRepository<WordStudyHistory, Long> {
    Optional<WordStudyHistory> findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
            String wordType, Integer seq, String word, String partOfSpeech, String meaning
    );

    List<WordStudyHistory> findByLastSeenAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("""
            select count(h)
            from WordStudyHistory h
            where h.wrongCount > h.correctCount
               or (h.lastWrongAt is not null and (h.lastCorrectAt is null or h.lastWrongAt > h.lastCorrectAt))
            """)
    long countReviewDue();

    @Query("""
            select h
            from WordStudyHistory h
            where h.wordType = :wordType
              and (h.wrongCount > h.correctCount
                   or (h.lastWrongAt is not null and (h.lastCorrectAt is null or h.lastWrongAt > h.lastCorrectAt)))
            order by (h.wrongCount - h.correctCount) desc, h.lastWrongAt desc
            """)
    List<WordStudyHistory> findReviewDue(String wordType, Pageable pageable);
}
