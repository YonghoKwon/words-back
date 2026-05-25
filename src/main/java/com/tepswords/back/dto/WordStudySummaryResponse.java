package com.tepswords.back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordStudySummaryResponse {
    private long seenCount;
    private long correctCount;
    private long wrongCount;
    private int accuracyRate;
    private long reviewDueCount;
}
