package com.tepswords.back.repository;

import com.tepswords.back.model.WordWrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordWrongAnswerRepository extends JpaRepository<WordWrongAnswer, Long> {
    Optional<WordWrongAnswer> findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
            String wordType, Integer seq, String word, String partOfSpeech, String meaning
    );

    List<WordWrongAnswer> findTop100ByOrderByLastWrongAtDesc();
}
