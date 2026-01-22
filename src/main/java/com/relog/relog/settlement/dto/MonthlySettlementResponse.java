package com.relog.relog.settlement.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MonthlySettlementResponse {

    private int year;
    private int month;
    private SummaryResponse summary;
    private TopFriendResponse topFriend;
    private RelationshipSolutionResponse solution;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class SummaryResponse {
        private int totalMeetings;
        private int positiveMeetings;
        private int negativeMeetings;
        private double averageScore;
        private int totalGiftsGiven;
        private int totalGiftsReceived;
        private long totalAmountGiven;
        private long totalAmountReceived;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class TopFriendResponse {
        private Long friendId;
        private String friendName;
        private int meetingCount;
        private double averageScore;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RelationshipSolutionResponse {
        private String friendName;
        private String analysis;
        private List<String> suggestions;
    }
}
