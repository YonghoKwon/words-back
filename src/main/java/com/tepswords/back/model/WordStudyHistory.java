package com.tepswords.back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "word_study_history", uniqueConstraints = {
        @UniqueConstraint(name = "uk_word_study_history_word", columnNames = {"word_type", "seq", "word", "part_of_speech", "meaning"})
})
@Getter
@Setter
@NoArgsConstructor
public class WordStudyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_type", nullable = false, length = 20)
    private String wordType;

    @Column(nullable = false)
    private Integer seq;

    @Column(nullable = false, length = 255)
    private String word;

    @Column(name = "part_of_speech", nullable = false, length = 30)
    private String partOfSpeech;

    @Column(nullable = false, length = 255)
    private String meaning;

    @Column(name = "seen_count", nullable = false)
    private Integer seenCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "last_correct_at")
    private LocalDateTime lastCorrectAt;

    @Column(name = "last_wrong_at")
    private LocalDateTime lastWrongAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (seenCount == null) seenCount = 0;
        if (correctCount == null) correctCount = 0;
        if (wrongCount == null) wrongCount = 0;
        if (lastSeenAt == null) lastSeenAt = now;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
