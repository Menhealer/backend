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
    private List<AnalyzedFriendResponse> bestFriends;
    private List<AnalyzedFriendResponse> worstFriends;
    private QuarterlySolutionResponse solution;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class AnalyzedFriendResponse {
        private FriendResponse friend;
        private String recommendation;
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