package com.tepswords.back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordActionRequest {
    private String wordType;
    private Integer seq;
    private String word;
    private String partOfSpeech;
    private String meaning;
}
