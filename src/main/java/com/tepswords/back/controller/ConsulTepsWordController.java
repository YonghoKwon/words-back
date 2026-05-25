package com.tepswords.back.controller;

import com.tepswords.back.dto.ApiWordDto;
import com.tepswords.back.model.ConsulTepsWord;
import com.tepswords.back.model.TepsWord;
import com.tepswords.back.service.ConsulTepsWordService;
import com.tepswords.back.service.TepsWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
public class ConsulTepsWordController {

    private static final int MAX_RANGE_SIZE = 200;
    private static final int DEFAULT_QUIZ_CHOICE_LIMIT = 8;
    private static final int MAX_QUIZ_CHOICE_LIMIT = 20;

    private final ConsulTepsWordService tepsWordService;
    private final TepsWordService regularWordService;

    @Autowired
    public ConsulTepsWordController(ConsulTepsWordService tepsWordService, TepsWordService regularWordService) {
        this.tepsWordService = tepsWordService;
        this.regularWordService = regularWordService;
    }

    // 모든 컨설텝스 단어 조회
    @GetMapping
    public ResponseEntity<List<ApiWordDto>> getAllWords() {
        return ResponseEntity.ok(tepsWordService.getAllWords().stream()
                .map(this::toApiWordDto)
                .toList());
    }

    // ID로 단어 조회
    @GetMapping("/{seq}/{word}/{partOfSpeech}/{meaning}")
    public ResponseEntity<?> getWordById(
            @PathVariable Integer seq,
            @PathVariable String word,
            @PathVariable String partOfSpeech,
            @PathVariable String meaning) {

        return tepsWordService.getWordById(seq, word, partOfSpeech, meaning)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 랜덤 단어 가져오기 (type: concepts | regular)
    @GetMapping("/random")
    public ResponseEntity<ApiWordDto> getRandomWord(
            @RequestParam(defaultValue = "concepts") String type,
            @RequestParam(required = false) String partOfSpeech
    ) {
        if ("regular".equalsIgnoreCase(type)) {
            TepsWord randomRegular = regularWordService.getRandomWord();
            if (randomRegular == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(toApiWordDto(randomRegular));
        }

        ConsulTepsWord randomWord = (partOfSpeech == null || partOfSpeech.isBlank())
                ? tepsWordService.getRandomWord()
                : tepsWordService.getRandomWordByPartOfSpeech(partOfSpeech);

        if (randomWord == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toApiWordDto(randomWord));
    }

    // seq 범위로 단어 조회 (예: 1~20, type: concepts | regular)
    @GetMapping("/range")
    public ResponseEntity<List<ApiWordDto>> getWordsBySeqRange(
            @RequestParam(defaultValue = "concepts") String type,
            @RequestParam(defaultValue = "1") Integer startSeq,
            @RequestParam(defaultValue = "20") Integer endSeq) {

        int safeStartSeq = Math.max(1, startSeq == null ? 1 : startSeq);
        int safeEndSeq = Math.max(safeStartSeq, endSeq == null ? safeStartSeq + 19 : endSeq);
        if (safeEndSeq - safeStartSeq + 1 > MAX_RANGE_SIZE) {
            safeEndSeq = safeStartSeq + MAX_RANGE_SIZE - 1;
        }

        if ("regular".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(regularWordService.getWordsBySeqRange(safeStartSeq, safeEndSeq).stream()
                    .map(this::toApiWordDto)
                    .toList());
        }

        return ResponseEntity.ok(tepsWordService.getWordsBySeqRange(safeStartSeq, safeEndSeq).stream()
                .map(this::toApiWordDto)
                .toList());
    }

    // 객관식 퀴즈 보기 생성용 후보 조회
    @GetMapping("/quiz-choices")
    public ResponseEntity<List<ApiWordDto>> getQuizChoices(
            @RequestParam(defaultValue = "concepts") String type,
            @RequestParam Integer seq,
            @RequestParam(required = false) String partOfSpeech,
            @RequestParam(defaultValue = "8") Integer limit) {

        int safeLimit = Math.max(2, Math.min(limit == null ? DEFAULT_QUIZ_CHOICE_LIMIT : limit, MAX_QUIZ_CHOICE_LIMIT));

        if ("regular".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(regularWordService.getQuizDistractors(seq, safeLimit).stream()
                    .map(this::toApiWordDto)
                    .toList());
        }

        return ResponseEntity.ok(tepsWordService.getQuizDistractors(seq, partOfSpeech, safeLimit).stream()
                .map(this::toApiWordDto)
                .toList());
    }

    // 하위 호환: 기존 경로 유지
    @GetMapping("/random/partOfSpeech/{partOfSpeech}")
    public ResponseEntity<ApiWordDto> getRandomWordByPartOfSpeech(
            @PathVariable String partOfSpeech,
            @RequestParam(defaultValue = "concepts") String type
    ) {
        return getRandomWord(type, partOfSpeech);
    }

    private ApiWordDto toApiWordDto(ConsulTepsWord word) {
        return new ApiWordDto(
                word.getSeq(),
                word.getWord(),
                word.getPartOfSpeech(),
                word.getMeaning()
        );
    }

    private ApiWordDto toApiWordDto(TepsWord word) {
        return new ApiWordDto(
                word.getSeq(),
                word.getWord(),
                "",
                word.getMeaning()
        );
    }
}
