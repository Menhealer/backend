package com.relog.relog.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QuarterlyAiRequest {

    private int year;
    private int quarter;
    private List<MonthlySummaryData> monthlySummaries;
    private List<FriendRankData> bestFriends;
    private List<FriendRankData> worstFriends;
    private List<FriendMaintenanceData> friendsToMaintain;
    private List<FriendMaintenanceData> friendsNeedingAttention;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class MonthlySummaryData {
        private int month;
        private int totalMeetings;
        private int positiveMeetings;
        private int negativeMeetings;
        private double averageScore;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class FriendRankData {
        private String friendName;
        private int meetingCount;
        private double averageScore;
        private int positiveCount;
        private int negativeCount;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class FriendMaintenanceData {
        private String friendName;
        private int daysSinceLastMeeting;
        private String recommendation;
    }
}
