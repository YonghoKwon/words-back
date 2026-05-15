package com.tepswords.back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordStudyResultRequest {
    private String wordType;
    private Integer seq;
    private String word;
    private String partOfSpeech;
    private String meaning;
    private String result;
}
