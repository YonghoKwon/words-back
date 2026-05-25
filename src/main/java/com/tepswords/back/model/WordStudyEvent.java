package com.tepswords.back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "word_study_event")
@Getter
@Setter
@NoArgsConstructor
public class WordStudyEvent {

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

    @Column(nullable = false, length = 20)
    private String result;

    @Column(name = "studied_at", nullable = false)
    private LocalDateTime studiedAt;

    @PrePersist
    public void prePersist() {
        if (studiedAt == null) {
            studiedAt = LocalDateTime.now();
        }
    }
}
