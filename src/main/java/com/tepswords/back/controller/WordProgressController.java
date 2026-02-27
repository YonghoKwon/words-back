package com.tepswords.back.controller;

import com.tepswords.back.dto.WordActionRequest;
import com.tepswords.back.dto.WordProgressResponse;
import com.tepswords.back.model.WordBookmark;
import com.tepswords.back.model.WordWrongAnswer;
import com.tepswords.back.service.WordProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
public class WordProgressController {

    private final WordProgressService wordProgressService;

    public WordProgressController(WordProgressService wordProgressService) {
        this.wordProgressService = wordProgressService;
    }

    @PostMapping("/bookmarks")
    public ResponseEntity<WordBookmark> addBookmark(@RequestBody WordActionRequest request) {
        return ResponseEntity.ok(wordProgressService.saveBookmark(request));
    }

    @DeleteMapping("/bookmarks")
    public ResponseEntity<Void> removeBookmark(
            @RequestParam String wordType,
            @RequestParam Integer seq,
            @RequestParam String word,
            @RequestParam String partOfSpeech,
            @RequestParam String meaning
    ) {
        WordActionRequest req = new WordActionRequest();
        req.setWordType(wordType);
        req.setSeq(seq);
        req.setWord(word);
        req.setPartOfSpeech(partOfSpeech);
        req.setMeaning(meaning);

        wordProgressService.removeBookmark(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wrongs")
    public ResponseEntity<WordWrongAnswer> markWrong(@RequestBody WordActionRequest request) {
        return ResponseEntity.ok(wordProgressService.markWrong(request));
    }

    @GetMapping("/progress")
    public ResponseEntity<WordProgressResponse> getProgress(
            @RequestParam String wordType,
            @RequestParam Integer seq,
            @RequestParam String word,
            @RequestParam String partOfSpeech,
            @RequestParam String meaning
    ) {
        WordActionRequest req = new WordActionRequest();
        req.setWordType(wordType);
        req.setSeq(seq);
        req.setWord(word);
        req.setPartOfSpeech(partOfSpeech);
        req.setMeaning(meaning);

        return ResponseEntity.ok(wordProgressService.getProgress(req));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<WordBookmark>> bookmarks() {
        return ResponseEntity.ok(wordProgressService.recentBookmarks());
    }

    @GetMapping("/wrongs")
    public ResponseEntity<List<WordWrongAnswer>> wrongs() {
        return ResponseEntity.ok(wordProgressService.recentWrongAnswers());
    }
}
