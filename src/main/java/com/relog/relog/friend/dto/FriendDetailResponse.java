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
    private Integer score;
    private List<EventSummaryResponse> recentEvents;
    private List<GiftSummaryResponse> giftHistory;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EventSummaryResponse {
        private Long eventId;
        private String title;
        private String eventDate;
        private String reviewScore;
        private String reviewText;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class GiftSummaryResponse {
        private Long giftId;
        private Integer price;
        private String giftDate;
        private String giftType;
        private String direction;
        private String description;
    }
}
