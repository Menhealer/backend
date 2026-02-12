package com.relog.relog.settlement.dto;

import com.relog.relog.friend.dto.FriendResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QuarterlySettlementResponse {

    private int year;
    private int quarter;
    private List<BestFriendResponse> bestFriends;
    private List<FriendRankResponse> worstFriends;
    private QuarterlySolutionResponse solution;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BestFriendResponse {
        private FriendResponse friend;
        private String recommendation;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class FriendRankResponse {
        private Long friendId;
        private String friendName;
        private int meetingCount;
        private double averageScore;
        private int positiveCount;
        private int negativeCount;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class QuarterlySolutionResponse {
        private String overallAnalysis;
        private List<String> positiveInsights;
        private List<String> negativeInsights;
        private List<String> actionItems;
    }
}