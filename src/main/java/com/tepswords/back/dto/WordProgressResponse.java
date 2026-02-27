package com.tepswords.back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordProgressResponse {
    private boolean bookmarked;
    private int wrongCount;
}
