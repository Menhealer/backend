package com.relog.relog.ai;

import com.relog.relog.ai.dto.MonthlyAnalysisResult;
import com.relog.relog.ai.dto.QuarterlyAnalysisResult;
import com.relog.relog.event.entity.Event;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.gift.entity.Gift;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DefaultAiAnalysisService implements AiAnalysisService {

    private static final int POSITIVE_THRESHOLD = 4;

    @Override
    public MonthlyAnalysisResult analyzeMonthly(List<Event> events, List<Gift> gifts) {
        if (events.isEmpty()) {
            return createEmptyMonthlyResult();
        }

        TopFriendInfo topFriend = findTopFriend(events);
        if (topFriend == null) {
            return createEmptyMonthlyResult();
        }

        return createMonthlyResultByScore(topFriend);
    }

    @Override
    public QuarterlyAnalysisResult analyzeQuarterly(List<Event> events, List<Friend> friends) {
        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);

        List<String> positiveInsights = buildPositiveInsights(eventsByFriend);
        List<String> negativeInsights = buildNegativeInsights(eventsByFriend);
        List<String> actionItems = buildActionItems(events, eventsByFriend);
        String overallAnalysis = buildOverallAnalysis(events.size());

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

    private MonthlyAnalysisResult createMonthlyResultByScore(TopFriendInfo topFriend) {
        double score = topFriend.averageScore;
        String friendName = topFriend.friendName;

        if (score >= 4.0) {
            return createPositiveResult(friendName);
        }
        if (score >= 3.0) {
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

    private TopFriendInfo findTopFriend(List<Event> events) {
        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);

        Long topFriendId = null;
        int maxCount = 0;

        for (Map.Entry<Long, List<Event>> entry : eventsByFriend.entrySet()) {
            if (entry.getValue().size() <= maxCount) {
                continue;
            }
            maxCount = entry.getValue().size();
            topFriendId = entry.getKey();
        }

        if (topFriendId == null) {
            return null;
        }

        List<Event> topFriendEvents = eventsByFriend.get(topFriendId);
        String friendName = topFriendEvents.get(0).getFriend().getName();
        double averageScore = calculateAverageScore(topFriendEvents);

        return new TopFriendInfo(topFriendId, friendName, maxCount, averageScore);
    }

    private Map<Long, List<Event>> groupEventsByFriend(List<Event> events) {
        Map<Long, List<Event>> result = new HashMap<>();

        for (Event event : events) {
            result.computeIfAbsent(event.getFriend().getId(), k -> new ArrayList<>()).add(event);
        }

        return result;
    }

    private double calculateAverageScore(List<Event> events) {
        int totalScore = 0;
        int count = 0;

        for (Event event : events) {
            if (event.getReviewScore() == null) {
                continue;
            }
            totalScore += event.getReviewScore().getScore();
            count++;
        }

        if (count == 0) {
            return 0.0;
        }
        return (double) totalScore / count;
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

    private List<String> buildPositiveInsights(Map<Long, List<Event>> eventsByFriend) {
        List<String> insights = new ArrayList<>();

        for (List<Event> friendEvents : eventsByFriend.values()) {
            double avgScore = calculateAverageScore(friendEvents);
            if (avgScore < POSITIVE_THRESHOLD) {
                continue;
            }
            String friendName = friendEvents.get(0).getFriend().getName();
            insights.add(friendName + "님과 좋은 관계를 유지하고 있습니다.");
        }

        return insights;
    }

    private List<String> buildNegativeInsights(Map<Long, List<Event>> eventsByFriend) {
        List<String> insights = new ArrayList<>();

        for (List<Event> friendEvents : eventsByFriend.values()) {
            double avgScore = calculateAverageScore(friendEvents);
            if (avgScore >= 3.0 || avgScore <= 0) {
                continue;
            }
            String friendName = friendEvents.get(0).getFriend().getName();
            insights.add(friendName + "님과의 관계 개선이 필요합니다.");
        }

        return insights;
    }

    private List<String> buildActionItems(List<Event> events, Map<Long, List<Event>> eventsByFriend) {
        List<String> actionItems = new ArrayList<>();

        if (events.isEmpty()) {
            actionItems.add("친구들과의 만남을 계획해보세요.");
        }

        for (List<Event> friendEvents : eventsByFriend.values()) {
            double avgScore = calculateAverageScore(friendEvents);
            if (avgScore >= 3.0 || avgScore <= 0) {
                continue;
            }
            String friendName = friendEvents.get(0).getFriend().getName();
            actionItems.add(friendName + "님과 솔직한 대화를 나눠보세요.");
        }

        return actionItems;
    }

    private record TopFriendInfo(Long friendId, String friendName, int meetingCount, double averageScore) {}
}
