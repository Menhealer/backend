package com.relog.relog.ai;

import com.relog.relog.ai.dto.MonthlyAiRequest;
import com.relog.relog.ai.dto.MonthlyAnalysisResult;
import com.relog.relog.ai.dto.QuarterlyAiRequest;
import com.relog.relog.ai.dto.QuarterlyAnalysisResult;

public interface AiAnalysisService {

    MonthlyAnalysisResult analyzeMonthly(MonthlyAiRequest request);

    QuarterlyAnalysisResult analyzeQuarterly(QuarterlyAiRequest request);
}
