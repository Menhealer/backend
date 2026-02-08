package com.relog.relog.ai;

import com.relog.relog.ai.dto.MonthlyAiRequest;
import com.relog.relog.ai.dto.MonthlyAnalysisResult;
import com.relog.relog.ai.dto.QuarterlyAiRequest;
import com.relog.relog.ai.dto.QuarterlyAnalysisResult;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Primary
public class PythonAiAnalysisService implements AiAnalysisService {

    private final WebClient webClient;
    private final DefaultAiAnalysisService fallbackService;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    public PythonAiAnalysisService(
            WebClient.Builder webClientBuilder,
            @Value("${ai.server.url}") String aiServerUrl,
            DefaultAiAnalysisService fallbackService) {
        this.webClient = webClientBuilder.baseUrl(aiServerUrl).build();
        this.fallbackService = fallbackService;
    }

    @Override
    public MonthlyAnalysisResult analyzeMonthly(MonthlyAiRequest request) {
        try {
            MonthlyAnalysisResult result = webClient.post()
                    .uri("/api/analyze/monthly")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(MonthlyAnalysisResult.class)
                    .timeout(TIMEOUT)
                    .block();

            if (result == null) {
                log.warn("[AI Server] Monthly analysis returned null, using fallback");
                return fallbackService.analyzeMonthly(request);
            }

            return result;
        } catch (Exception e) {
            log.warn("[AI Server] Monthly analysis failed, using fallback: {}", e.getMessage());
            return fallbackService.analyzeMonthly(request);
        }
    }

    @Override
    public QuarterlyAnalysisResult analyzeQuarterly(QuarterlyAiRequest request) {
        try {
            QuarterlyAnalysisResult result = webClient.post()
                    .uri("/api/analyze/quarterly")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(QuarterlyAnalysisResult.class)
                    .timeout(TIMEOUT)
                    .block();

            if (result == null) {
                log.warn("[AI Server] Quarterly analysis returned null, using fallback");
                return fallbackService.analyzeQuarterly(request);
            }

            return result;
        } catch (Exception e) {
            log.warn("[AI Server] Quarterly analysis failed, using fallback: {}", e.getMessage());
            return fallbackService.analyzeQuarterly(request);
        }
    }
}
