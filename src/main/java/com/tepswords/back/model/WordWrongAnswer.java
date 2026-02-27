package com.tepswords.back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "word_wrong_answer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_word_wrong_word", columnNames = {"word_type", "seq", "word", "part_of_speech", "meaning"})
})
@Getter
@Setter
@NoArgsConstructor
public class WordWrongAnswer {

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

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;

    @Column(name = "last_wrong_at", nullable = false)
    private LocalDateTime lastWrongAt;

    @PrePersist
    public void prePersist() {
        if (wrongCount == null) {
            wrongCount = 1;
        }
        if (lastWrongAt == null) {
            lastWrongAt = LocalDateTime.now();
        }
    }
}
