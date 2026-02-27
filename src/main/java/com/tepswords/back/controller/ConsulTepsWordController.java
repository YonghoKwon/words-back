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

    private final ConsulTepsWordService tepsWordService;
    private final TepsWordService regularWordService;

    @Autowired
    public ConsulTepsWordController(ConsulTepsWordService tepsWordService, TepsWordService regularWordService) {
        this.tepsWordService = tepsWordService;
        this.regularWordService = regularWordService;
    }

    // 모든 단어 조회
    @GetMapping
    public ResponseEntity<List<ConsulTepsWord>> getAllWords() {
        List<ConsulTepsWord> words = tepsWordService.getAllWords();
        return ResponseEntity.ok(words);
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
            return ResponseEntity.ok(new ApiWordDto(
                    randomRegular.getSeq(),
                    randomRegular.getWord(),
                    "",
                    randomRegular.getMeaning()
            ));
        }

        ConsulTepsWord randomWord = (partOfSpeech == null || partOfSpeech.isBlank())
                ? tepsWordService.getRandomWord()
                : tepsWordService.getRandomWordByPartOfSpeech(partOfSpeech);

        if (randomWord == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new ApiWordDto(
                randomWord.getSeq(),
                randomWord.getWord(),
                randomWord.getPartOfSpeech(),
                randomWord.getMeaning()
        ));
    }

    // seq 범위로 단어 조회 (예: 1~20)
    @GetMapping("/range")
    public ResponseEntity<List<ConsulTepsWord>> getWordsBySeqRange(
            @RequestParam(defaultValue = "1") Integer startSeq,
            @RequestParam(defaultValue = "20") Integer endSeq) {

        // 범위 유효성 검사
        if (startSeq < 1) {
            startSeq = 1;
        }

        if (endSeq < startSeq) {
            endSeq = startSeq;
        }

        List<ConsulTepsWord> words = tepsWordService.getWordsBySeqRange(startSeq, endSeq);
        return ResponseEntity.ok(words);
    }

    // 하위 호환: 기존 경로 유지
    @GetMapping("/random/partOfSpeech/{partOfSpeech}")
    public ResponseEntity<ApiWordDto> getRandomWordByPartOfSpeech(
            @PathVariable String partOfSpeech,
            @RequestParam(defaultValue = "concepts") String type
    ) {
        return getRandomWord(type, partOfSpeech);
    }
}

