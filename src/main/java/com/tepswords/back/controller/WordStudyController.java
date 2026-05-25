package com.tepswords.back.controller;

import com.tepswords.back.dto.ReviewDueWordResponse;
import com.tepswords.back.dto.WordStudyResultRequest;
import com.tepswords.back.dto.WordStudySummaryResponse;
import com.tepswords.back.model.WordStudyHistory;
import com.tepswords.back.service.WordStudyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/words")
public class WordStudyController {

    private final WordStudyService wordStudyService;

    public WordStudyController(WordStudyService wordStudyService) {
        this.wordStudyService = wordStudyService;
    }

    @PostMapping("/study-results")
    public ResponseEntity<WordStudyHistory> recordStudyResult(@RequestBody WordStudyResultRequest request) {
        return ResponseEntity.ok(wordStudyService.recordResult(request));
    }

    @GetMapping("/study-summary")
    public ResponseEntity<WordStudySummaryResponse> studySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(wordStudyService.getSummary(date));
    }

    @GetMapping("/review-due")
    public ResponseEntity<List<ReviewDueWordResponse>> reviewDue(
            @RequestParam String type,
            @RequestParam(defaultValue = "30") int limit
    ) {
        return ResponseEntity.ok(wordStudyService.reviewDueWords(type, limit));
    }
}
