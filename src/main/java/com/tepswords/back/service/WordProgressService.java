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

    private static final String EMPTY_PART_OF_SPEECH = "-";

    private final WordBookmarkRepository wordBookmarkRepository;
    private final WordWrongAnswerRepository wordWrongAnswerRepository;

    public WordProgressService(WordBookmarkRepository wordBookmarkRepository, WordWrongAnswerRepository wordWrongAnswerRepository) {
        this.wordBookmarkRepository = wordBookmarkRepository;
        this.wordWrongAnswerRepository = wordWrongAnswerRepository;
    }

    public WordBookmark saveBookmark(WordActionRequest request) {
        normalizeAndValidateRequest(request);

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
        normalizeAndValidateRequest(request);

        wordBookmarkRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .ifPresent(wordBookmarkRepository::delete);
    }

    public WordWrongAnswer markWrong(WordActionRequest request) {
        normalizeAndValidateRequest(request);

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
        normalizeAndValidateRequest(request);

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

    private void normalizeAndValidateRequest(WordActionRequest request) {
        if (request == null || request.getSeq() == null ||
                !StringUtils.hasText(request.getWordType()) ||
                !StringUtils.hasText(request.getWord()) ||
                !StringUtils.hasText(request.getMeaning())) {
            throw new IllegalArgumentException("wordType, seq, word, meaning 값이 필요합니다.");
        }

        if (!StringUtils.hasText(request.getPartOfSpeech())) {
            request.setPartOfSpeech(EMPTY_PART_OF_SPEECH);
        }

        request.setWordType(request.getWordType().trim());
        request.setWord(request.getWord().trim());
        request.setPartOfSpeech(request.getPartOfSpeech().trim());
        request.setMeaning(request.getMeaning().trim());
    }
}
