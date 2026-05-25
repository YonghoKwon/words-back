package com.tepswords.back.repository;

import com.tepswords.back.model.ConsulTepsWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsulTepsWordRepository extends JpaRepository<ConsulTepsWord, ConsulTepsWord.TepsWordId> {

    // 단어로 검색
    List<ConsulTepsWord> findByWordContaining(String word);

    // 품사로 검색
    List<ConsulTepsWord> findByPartOfSpeech(String partOfSpeech);

    // seq로 검색
    List<ConsulTepsWord> findBySeq(Integer seq);

    // 랜덤 단어 가져오기
    @Query(value = "SELECT * FROM consulteps_words ORDER BY RAND() LIMIT 1", nativeQuery = true)
    ConsulTepsWord findRandomWord();

    // seq 범위로 검색
    List<ConsulTepsWord> findBySeqBetweenOrderBySeqAsc(Integer startSeq, Integer endSeq);

    @Query(value = "SELECT * FROM consulteps_words WHERE part_of_speech = :partOfSpeech ORDER BY RAND() LIMIT 1", nativeQuery = true)
    ConsulTepsWord findRandomWordByPartOfSpeech(@Param("partOfSpeech") String partOfSpeech);

    @Query(value = """
            SELECT *
            FROM consulteps_words
            WHERE seq <> :seq
              AND (:partOfSpeech IS NULL OR :partOfSpeech = '' OR part_of_speech = :partOfSpeech)
            ORDER BY RAND()
            LIMIT :limit
            """, nativeQuery = true)
    List<ConsulTepsWord> findQuizDistractors(
            @Param("seq") Integer seq,
            @Param("partOfSpeech") String partOfSpeech,
            @Param("limit") int limit
    );
}
