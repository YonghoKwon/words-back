package com.tepswords.back.service;

import com.tepswords.back.model.TepsWord;
import com.tepswords.back.repository.TepsWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TepsWordService {

    private final TepsWordRepository tepsWordRepository;

    @Autowired
    public TepsWordService(TepsWordRepository tepsWordRepository) {
        this.tepsWordRepository = tepsWordRepository;
    }

    // 모든 단어 조회
    public List<TepsWord> getAllWords() {
        return tepsWordRepository.findAll();
    }

    // ID(seq)로 단어 조회
    public Optional<TepsWord> getWordById(Integer seq) {
        return tepsWordRepository.findById(seq);
    }

    // 단어로 검색
    public List<TepsWord> searchByWord(String word) {
        return tepsWordRepository.findByWordContaining(word);
    }

    // 설명으로 검색
    public List<TepsWord> searchByDescription(String description) {
        return tepsWordRepository.findByDescriptionContaining(description);
    }

    // seq 범위로 단어 조회
    public List<TepsWord> getWordsBySeqRange(Integer startSeq, Integer endSeq) {
        return tepsWordRepository.findBySeqBetween(startSeq, endSeq);
    }

    // 랜덤 단어 가져오기
    public TepsWord getRandomWord() {
        return tepsWordRepository.findRandomWord().orElse(null);
    }

    public List<TepsWord> getQuizDistractors(Integer seq, int limit) {
        return tepsWordRepository.findQuizDistractors(seq, limit);
    }

    // 단어 저장(추가/수정)
    public TepsWord saveWord(TepsWord tepsWord) {
        return tepsWordRepository.save(tepsWord);
    }

    // 단어 삭제
    public void deleteWord(Integer seq) {
        tepsWordRepository.deleteById(seq);
    }
}