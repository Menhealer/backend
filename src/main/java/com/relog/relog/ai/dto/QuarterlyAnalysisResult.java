package com.relog.relog.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QuarterlyAnalysisResult {

    private String overallAnalysis;
    private List<String> positiveInsights;
    private List<String> negativeInsights;
    private List<String> actionItems;
}
