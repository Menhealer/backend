package com.relog.relog.ai;

import com.relog.relog.ai.dto.MonthlyAiRequest;
import com.relog.relog.ai.dto.MonthlyAnalysisResult;
import com.relog.relog.ai.dto.QuarterlyAiRequest;
import com.relog.relog.ai.dto.QuarterlyAiRequest.FriendRankData;
import com.relog.relog.ai.dto.QuarterlyAnalysisResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultAiAnalysisService {

    private static final double POSITIVE_THRESHOLD = 4.0;

    public MonthlyAnalysisResult analyzeMonthly(MonthlyAiRequest request) {
        if (request.getFriendName() == null || request.getEvents().isEmpty()) {
            return createEmptyMonthlyResult();
        }

        return createMonthlyResultByScore(request.getFriendName(), request.getSummary().getAverageScore());
    }

    public QuarterlyAnalysisResult analyzeQuarterly(QuarterlyAiRequest request) {
        int totalMeetings = request.getMonthlySummaries().stream()
                .mapToInt(QuarterlyAiRequest.MonthlySummaryData::getTotalMeetings)
                .sum();

        List<String> positiveInsights = buildPositiveInsights(request.getBestFriends());
        List<String> negativeInsights = buildNegativeInsights(request.getWorstFriends());
        List<String> actionItems = buildActionItems(totalMeetings, request.getWorstFriends());
        String overallAnalysis = buildOverallAnalysis(totalMeetings);

        return QuarterlyAnalysisResult.builder()
                .overallAnalysis(overallAnalysis)
                .positiveInsights(positiveInsights)
                .negativeInsights(negativeInsights)
                .actionItems(actionItems)
                .build();
    }

    private MonthlyAnalysisResult createEmptyMonthlyResult() {
        return MonthlyAnalysisResult.builder()
                .friendName(null)
                .analysis("이번 달 만남 기록이 없습니다.")
                .suggestions(List.of("친구들과의 만남을 계획해보세요."))
                .build();
    }

    private MonthlyAnalysisResult createMonthlyResultByScore(String friendName, double averageScore) {
        if (averageScore >= 4.0) {
            return createPositiveResult(friendName);
        }
        if (averageScore >= 3.0) {
            return createNeutralResult(friendName);
        }
        return createNegativeResult(friendName);
    }

    private MonthlyAnalysisResult createPositiveResult(String friendName) {
        return MonthlyAnalysisResult.builder()
                .friendName(friendName)
                .analysis(friendName + "님과의 관계가 매우 좋습니다.")
                .suggestions(List.of("이 관계를 계속 유지하세요.", "특별한 날에 선물을 준비해보세요."))
                .build();
    }

    private MonthlyAnalysisResult createNeutralResult(String friendName) {
        return MonthlyAnalysisResult.builder()
                .friendName(friendName)
                .analysis(friendName + "님과의 관계는 보통입니다.")
                .suggestions(List.of("더 깊은 대화를 나눠보세요.", "함께 새로운 활동을 시도해보세요."))
                .build();
    }

    private MonthlyAnalysisResult createNegativeResult(String friendName) {
        return MonthlyAnalysisResult.builder()
                .friendName(friendName)
                .analysis(friendName + "님과의 관계에 주의가 필요합니다.")
                .suggestions(List.of("솔직한 대화를 통해 문제를 파악해보세요.", "관계 개선을 위한 노력이 필요합니다."))
                .build();
    }

    private String buildOverallAnalysis(int totalMeetings) {
        if (totalMeetings == 0) {
            return "이번 분기 만남 기록이 없습니다.";
        }
        if (totalMeetings < 5) {
            return "이번 분기 만남이 적은 편입니다.";
        }
        return "이번 분기 활발한 교류가 있었습니다.";
    }

    private List<String> buildPositiveInsights(List<FriendRankData> bestFriends) {
        List<String> insights = new ArrayList<>();

        for (FriendRankData friend : bestFriends) {
            if (friend.getAverageScore() < POSITIVE_THRESHOLD) {
                continue;
            }
            insights.add(friend.getFriendName() + "님과 좋은 관계를 유지하고 있습니다.");
        }

        return insights;
    }

    private List<String> buildNegativeInsights(List<FriendRankData> worstFriends) {
        List<String> insights = new ArrayList<>();

        for (FriendRankData friend : worstFriends) {
            if (friend.getAverageScore() >= 3.0 || friend.getAverageScore() <= 0) {
                continue;
            }
            insights.add(friend.getFriendName() + "님과의 관계 개선이 필요합니다.");
        }

        return insights;
    }

    private List<String> buildActionItems(int totalMeetings, List<FriendRankData> worstFriends) {
        List<String> actionItems = new ArrayList<>();

        if (totalMeetings == 0) {
            actionItems.add("친구들과의 만남을 계획해보세요.");
        }

        for (FriendRankData friend : worstFriends) {
            if (friend.getAverageScore() >= 3.0 || friend.getAverageScore() <= 0) {
                continue;
            }
            actionItems.add(friend.getFriendName() + "님과 솔직한 대화를 나눠보세요.");
        }

        return actionItems;
    }
}
