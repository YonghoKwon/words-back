package com.tepswords.back.service;

import com.tepswords.back.model.ConsulTepsWord;
import com.tepswords.back.repository.ConsulTepsWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsulTepsWordService {

    private final ConsulTepsWordRepository tepsWordRepository;

    @Autowired
    public ConsulTepsWordService(ConsulTepsWordRepository tepsWordRepository) {
        this.tepsWordRepository = tepsWordRepository;
    }

    // 모든 단어 조회
    public List<ConsulTepsWord> getAllWords() {
        return tepsWordRepository.findAll();
    }

    // 단어로 검색
    public List<ConsulTepsWord> searchByWord(String word) {
        return tepsWordRepository.findByWordContaining(word);
    }

    // 품사로 검색
    public List<ConsulTepsWord> searchByPartOfSpeech(String partOfSpeech) {
        return tepsWordRepository.findByPartOfSpeech(partOfSpeech);
    }

    public Optional<ConsulTepsWord> getWordById(Integer seq, String word, String partOfSpeech, String meaning) {
        ConsulTepsWord.TepsWordId id = new ConsulTepsWord.TepsWordId(seq, word, partOfSpeech, meaning);
        return tepsWordRepository.findById(id);
    }

    // 랜덤 단어 가져오기
    public ConsulTepsWord getRandomWord() {
        return tepsWordRepository.findRandomWord();
    }

    // seq 범위로 단어 조회
    public List<ConsulTepsWord> getWordsBySeqRange(Integer startSeq, Integer endSeq) {
        return tepsWordRepository.findBySeqBetweenOrderBySeqAsc(startSeq, endSeq);
    }

    public ConsulTepsWord getRandomWordByPartOfSpeech(String partOfSpeech) {
        return tepsWordRepository.findRandomWordByPartOfSpeech(partOfSpeech);
    }

    public List<ConsulTepsWord> getQuizDistractors(Integer seq, String partOfSpeech, int limit) {
        return tepsWordRepository.findQuizDistractors(seq, partOfSpeech, limit);
    }
}
