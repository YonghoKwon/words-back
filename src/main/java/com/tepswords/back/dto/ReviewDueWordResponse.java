package com.tepswords.back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDueWordResponse {
    private Integer seq;
    private String word;
    private String partOfSpeech;
    private String meaning;
}
