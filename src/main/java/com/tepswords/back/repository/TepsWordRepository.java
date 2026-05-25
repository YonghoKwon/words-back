package com.tepswords.back.repository;

import com.tepswords.back.model.TepsWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TepsWordRepository extends JpaRepository<TepsWord, Integer> {

    // 단어로 검색
    List<TepsWord> findByWordContaining(String word);

    // 설명으로 검색
    List<TepsWord> findByDescriptionContaining(String description);

    // seq 범위로 검색
    List<TepsWord> findBySeqBetween(Integer startSeq, Integer endSeq);

    // 랜덤 단어 가져오기
    @Query(value = "SELECT * FROM teps_words ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<TepsWord> findRandomWord();

    @Query(value = """
            SELECT *
            FROM teps_words
            WHERE seq <> :seq
            ORDER BY RAND()
            LIMIT :limit
            """, nativeQuery = true)
    List<TepsWord> findQuizDistractors(@Param("seq") Integer seq, @Param("limit") int limit);
}