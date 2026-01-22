package com.relog.relog.friend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FriendDetailResponse {

    private FriendResponse friend;
    private RelationshipScoreResponse relationshipScore;
    private List<EventSummaryResponse> recentEvents;
    private List<GiftSummaryResponse> giftHistory;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class RelationshipScoreResponse {
        private int totalMeetings;
        private double averageScore;
        private int positiveCount;
        private int negativeCount;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EventSummaryResponse {
        private Long eventId;
        private String title;
        private String eventDate;
        private String reviewScore;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class GiftSummaryResponse {
        private Long giftId;
        private String itemName;
        private Integer price;
        private String giftDate;
        private String giftType;
        private String direction;
    }
}
