package com.tepswords.back.repository;

import com.tepswords.back.model.WordBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordBookmarkRepository extends JpaRepository<WordBookmark, Long> {
    Optional<WordBookmark> findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
            String wordType, Integer seq, String word, String partOfSpeech, String meaning
    );

    List<WordBookmark> findTop100ByOrderByCreatedAtDesc();
}
