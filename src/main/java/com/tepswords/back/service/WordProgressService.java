package com.tepswords.back.service;

import com.tepswords.back.dto.WordActionRequest;
import com.tepswords.back.dto.WordProgressResponse;
import com.tepswords.back.model.WordBookmark;
import com.tepswords.back.model.WordWrongAnswer;
import com.tepswords.back.repository.WordBookmarkRepository;
import com.tepswords.back.repository.WordWrongAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WordProgressService {

    private final WordBookmarkRepository wordBookmarkRepository;
    private final WordWrongAnswerRepository wordWrongAnswerRepository;

    public WordProgressService(WordBookmarkRepository wordBookmarkRepository, WordWrongAnswerRepository wordWrongAnswerRepository) {
        this.wordBookmarkRepository = wordBookmarkRepository;
        this.wordWrongAnswerRepository = wordWrongAnswerRepository;
    }

    public WordBookmark saveBookmark(WordActionRequest request) {
        validateRequest(request);

        return wordBookmarkRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .orElseGet(() -> {
                    WordBookmark bookmark = new WordBookmark();
                    bookmark.setWordType(request.getWordType());
                    bookmark.setSeq(request.getSeq());
                    bookmark.setWord(request.getWord());
                    bookmark.setPartOfSpeech(request.getPartOfSpeech());
                    bookmark.setMeaning(request.getMeaning());
                    return wordBookmarkRepository.save(bookmark);
                });
    }

    public void removeBookmark(WordActionRequest request) {
        validateRequest(request);

        wordBookmarkRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .ifPresent(wordBookmarkRepository::delete);
    }

    public WordWrongAnswer markWrong(WordActionRequest request) {
        validateRequest(request);

        return wordWrongAnswerRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .map(existing -> {
                    existing.setWrongCount(existing.getWrongCount() + 1);
                    existing.setLastWrongAt(LocalDateTime.now());
                    return wordWrongAnswerRepository.save(existing);
                })
                .orElseGet(() -> {
                    WordWrongAnswer wrongAnswer = new WordWrongAnswer();
                    wrongAnswer.setWordType(request.getWordType());
                    wrongAnswer.setSeq(request.getSeq());
                    wrongAnswer.setWord(request.getWord());
                    wrongAnswer.setPartOfSpeech(request.getPartOfSpeech());
                    wrongAnswer.setMeaning(request.getMeaning());
                    wrongAnswer.setWrongCount(1);
                    wrongAnswer.setLastWrongAt(LocalDateTime.now());
                    return wordWrongAnswerRepository.save(wrongAnswer);
                });
    }

    public WordProgressResponse getProgress(WordActionRequest request) {
        validateRequest(request);

        boolean bookmarked = wordBookmarkRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .isPresent();

        int wrongCount = wordWrongAnswerRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .map(WordWrongAnswer::getWrongCount)
                .orElse(0);

        return new WordProgressResponse(bookmarked, wrongCount);
    }

    public List<WordBookmark> recentBookmarks() {
        return wordBookmarkRepository.findTop100ByOrderByCreatedAtDesc();
    }

    public List<WordWrongAnswer> recentWrongAnswers() {
        return wordWrongAnswerRepository.findTop100ByOrderByLastWrongAtDesc();
    }

    private void validateRequest(WordActionRequest request) {
        if (request == null || request.getSeq() == null ||
                !StringUtils.hasText(request.getWordType()) ||
                !StringUtils.hasText(request.getWord()) ||
                !StringUtils.hasText(request.getPartOfSpeech()) ||
                !StringUtils.hasText(request.getMeaning())) {
            throw new IllegalArgumentException("wordType, seq, word, partOfSpeech, meaning 값이 모두 필요합니다.");
        }
    }
}
