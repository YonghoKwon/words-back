package com.tepswords.back.service;

import com.tepswords.back.dto.ReviewDueWordResponse;
import com.tepswords.back.dto.WordStudyResultRequest;
import com.tepswords.back.dto.WordStudySummaryResponse;
import com.tepswords.back.model.WordStudyEvent;
import com.tepswords.back.model.WordStudyHistory;
import com.tepswords.back.repository.WordStudyEventRepository;
import com.tepswords.back.repository.WordStudyHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WordStudyService {

    private static final String EMPTY_PART_OF_SPEECH = "-";
    private static final String RESULT_CORRECT = "correct";
    private static final String RESULT_WRONG = "wrong";

    private final WordStudyHistoryRepository wordStudyHistoryRepository;
    private final WordStudyEventRepository wordStudyEventRepository;

    public WordStudyService(WordStudyHistoryRepository wordStudyHistoryRepository, WordStudyEventRepository wordStudyEventRepository) {
        this.wordStudyHistoryRepository = wordStudyHistoryRepository;
        this.wordStudyEventRepository = wordStudyEventRepository;
    }

    public WordStudyHistory recordResult(WordStudyResultRequest request) {
        normalizeAndValidateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        wordStudyEventRepository.save(toEvent(request, now));

        return wordStudyHistoryRepository
                .findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning(
                        request.getWordType(), request.getSeq(), request.getWord(), request.getPartOfSpeech(), request.getMeaning()
                )
                .map(existing -> {
                    applyResult(existing, request.getResult(), now);
                    return wordStudyHistoryRepository.save(existing);
                })
                .orElseGet(() -> {
                    WordStudyHistory history = new WordStudyHistory();
                    history.setWordType(request.getWordType());
                    history.setSeq(request.getSeq());
                    history.setWord(request.getWord());
                    history.setPartOfSpeech(request.getPartOfSpeech());
                    history.setMeaning(request.getMeaning());
                    history.setSeenCount(0);
                    history.setCorrectCount(0);
                    history.setWrongCount(0);
                    history.setCreatedAt(now);
                    applyResult(history, request.getResult(), now);
                    return wordStudyHistoryRepository.save(history);
                });
    }

    public WordStudySummaryResponse getSummary(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();
        List<WordStudyEvent> events = wordStudyEventRepository.findByStudiedAtBetween(start, end);

        long seenCount = events.size();
        long correctCount = events.stream().filter(event -> RESULT_CORRECT.equals(event.getResult())).count();
        long wrongCount = events.stream().filter(event -> RESULT_WRONG.equals(event.getResult())).count();
        int accuracyRate = seenCount > 0 ? Math.round((correctCount * 100f) / seenCount) : 0;
        long reviewDueCount = wordStudyHistoryRepository.countReviewDue();

        return new WordStudySummaryResponse(seenCount, correctCount, wrongCount, accuracyRate, reviewDueCount);
    }

    public List<ReviewDueWordResponse> reviewDueWords(String wordType, int limit) {
        if (!StringUtils.hasText(wordType)) {
            throw new IllegalArgumentException("wordType 값이 필요합니다.");
        }

        int normalizedLimit = Math.max(1, Math.min(limit, 100));
        return wordStudyHistoryRepository.findReviewDue(wordType.trim(), PageRequest.of(0, normalizedLimit))
                .stream()
                .map(history -> new ReviewDueWordResponse(
                        history.getSeq(),
                        history.getWord(),
                        denormalizePartOfSpeech(history.getPartOfSpeech()),
                        history.getMeaning()
                ))
                .toList();
    }

    private void applyResult(WordStudyHistory history, String result, LocalDateTime now) {
        history.setSeenCount(history.getSeenCount() + 1);
        history.setLastSeenAt(now);
        history.setUpdatedAt(now);

        if (RESULT_CORRECT.equals(result)) {
            history.setCorrectCount(history.getCorrectCount() + 1);
            history.setLastCorrectAt(now);
            return;
        }

        history.setWrongCount(history.getWrongCount() + 1);
        history.setLastWrongAt(now);
    }

    private WordStudyEvent toEvent(WordStudyResultRequest request, LocalDateTime now) {
        WordStudyEvent event = new WordStudyEvent();
        event.setWordType(request.getWordType());
        event.setSeq(request.getSeq());
        event.setWord(request.getWord());
        event.setPartOfSpeech(request.getPartOfSpeech());
        event.setMeaning(request.getMeaning());
        event.setResult(request.getResult());
        event.setStudiedAt(now);
        return event;
    }

    private void normalizeAndValidateRequest(WordStudyResultRequest request) {
        if (request == null || request.getSeq() == null ||
                !StringUtils.hasText(request.getWordType()) ||
                !StringUtils.hasText(request.getWord()) ||
                !StringUtils.hasText(request.getMeaning()) ||
                !StringUtils.hasText(request.getResult())) {
            throw new IllegalArgumentException("wordType, seq, word, meaning, result 값이 필요합니다.");
        }

        request.setResult(request.getResult().trim());
        if (!RESULT_CORRECT.equals(request.getResult()) && !RESULT_WRONG.equals(request.getResult())) {
            throw new IllegalArgumentException("result 값은 correct 또는 wrong 이어야 합니다.");
        }

        if (!StringUtils.hasText(request.getPartOfSpeech())) {
            request.setPartOfSpeech(EMPTY_PART_OF_SPEECH);
        }

        request.setWordType(request.getWordType().trim());
        request.setWord(request.getWord().trim());
        request.setPartOfSpeech(request.getPartOfSpeech().trim());
        request.setMeaning(request.getMeaning().trim());
    }

    private String denormalizePartOfSpeech(String partOfSpeech) {
        return EMPTY_PART_OF_SPEECH.equals(partOfSpeech) ? "" : partOfSpeech;
    }
}
