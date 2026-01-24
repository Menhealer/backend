package com.relog.relog.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MonthlyAnalysisResult {

    private String friendName;
    private String analysis;
    private List<String> suggestions;
}
