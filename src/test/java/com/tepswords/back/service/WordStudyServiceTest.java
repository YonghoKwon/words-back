package com.tepswords.back.service;

import com.tepswords.back.dto.WordStudyResultRequest;
import com.tepswords.back.model.WordStudyEvent;
import com.tepswords.back.model.WordStudyHistory;
import com.tepswords.back.repository.WordStudyEventRepository;
import com.tepswords.back.repository.WordStudyHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordStudyServiceTest {

    private final WordStudyHistoryRepository historyRepository = mock(WordStudyHistoryRepository.class);
    private final WordStudyEventRepository eventRepository = mock(WordStudyEventRepository.class);
    private final WordStudyService service = new WordStudyService(historyRepository, eventRepository);

    @Test
    void recordResultCreatesHistoryAndEventWithNormalizedPartOfSpeech() {
        WordStudyResultRequest request = request("regular", 1, "word", "", "뜻", "correct");
        when(historyRepository.findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning("regular", 1, "word", "-", "뜻"))
                .thenReturn(Optional.empty());
        when(historyRepository.save(any(WordStudyHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(any(WordStudyEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordStudyHistory saved = service.recordResult(request);

        assertThat(saved.getSeenCount()).isEqualTo(1);
        assertThat(saved.getCorrectCount()).isEqualTo(1);
        assertThat(saved.getWrongCount()).isZero();
        assertThat(saved.getPartOfSpeech()).isEqualTo("-");

        ArgumentCaptor<WordStudyEvent> eventCaptor = ArgumentCaptor.forClass(WordStudyEvent.class);
        verify(eventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getResult()).isEqualTo("correct");
        assertThat(eventCaptor.getValue().getPartOfSpeech()).isEqualTo("-");
    }

    @Test
    void recordResultUpdatesExistingHistory() {
        WordStudyHistory existing = history("concepts", 2, "miss", "v.", "놓치다", 3, 2, 1);
        WordStudyResultRequest request = request("concepts", 2, "miss", "v.", "놓치다", "wrong");
        when(historyRepository.findByWordTypeAndSeqAndWordAndPartOfSpeechAndMeaning("concepts", 2, "miss", "v.", "놓치다"))
                .thenReturn(Optional.of(existing));
        when(historyRepository.save(any(WordStudyHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(any(WordStudyEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordStudyHistory saved = service.recordResult(request);

        assertThat(saved.getSeenCount()).isEqualTo(4);
        assertThat(saved.getCorrectCount()).isEqualTo(2);
        assertThat(saved.getWrongCount()).isEqualTo(2);
        assertThat(saved.getLastWrongAt()).isNotNull();
    }

    @Test
    void getSummaryUsesDailyEventsAndReviewDueCount() {
        LocalDate date = LocalDate.of(2026, 5, 15);
        WordStudyEvent correct = event("correct");
        WordStudyEvent wrong = event("wrong");
        when(eventRepository.findByStudiedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(correct, wrong, wrong));
        when(historyRepository.countReviewDue()).thenReturn(7L);

        var summary = service.getSummary(date);

        assertThat(summary.getSeenCount()).isEqualTo(3);
        assertThat(summary.getCorrectCount()).isEqualTo(1);
        assertThat(summary.getWrongCount()).isEqualTo(2);
        assertThat(summary.getAccuracyRate()).isEqualTo(33);
        assertThat(summary.getReviewDueCount()).isEqualTo(7);
    }

    @Test
    void reviewDueWordsReturnsDenormalizedPartOfSpeech() {
        WordStudyHistory history = history("regular", 1, "term", "-", "용어", 3, 0, 3);
        when(historyRepository.findReviewDue(eq("regular"), any(Pageable.class))).thenReturn(List.of(history));

        var words = service.reviewDueWords("regular", 30);

        assertThat(words).hasSize(1);
        assertThat(words.get(0).getPartOfSpeech()).isEmpty();
    }

    private WordStudyResultRequest request(String wordType, Integer seq, String word, String partOfSpeech, String meaning, String result) {
        WordStudyResultRequest request = new WordStudyResultRequest();
        request.setWordType(wordType);
        request.setSeq(seq);
        request.setWord(word);
        request.setPartOfSpeech(partOfSpeech);
        request.setMeaning(meaning);
        request.setResult(result);
        return request;
    }

    private WordStudyHistory history(String wordType, Integer seq, String word, String partOfSpeech, String meaning, int seen, int correct, int wrong) {
        WordStudyHistory history = new WordStudyHistory();
        history.setWordType(wordType);
        history.setSeq(seq);
        history.setWord(word);
        history.setPartOfSpeech(partOfSpeech);
        history.setMeaning(meaning);
        history.setSeenCount(seen);
        history.setCorrectCount(correct);
        history.setWrongCount(wrong);
        history.setLastSeenAt(LocalDateTime.now());
        return history;
    }

    private WordStudyEvent event(String result) {
        WordStudyEvent event = new WordStudyEvent();
        event.setResult(result);
        return event;
    }
}
