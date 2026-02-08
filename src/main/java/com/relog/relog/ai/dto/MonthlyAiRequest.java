package com.relog.relog.ai.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MonthlyAiRequest {

    private String friendName;
    private List<EventData> events;
    private List<GiftData> gifts;
    private SummaryData summary;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EventData {
        private LocalDate eventDate;
        private int reviewScore;
        private String reviewText;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class GiftData {
        private LocalDate giftDate;
        private String giftType;
        private String direction;
        private Integer price;
        private String description;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class SummaryData {
        private int totalMeetings;
        private int positiveMeetings;
        private int negativeMeetings;
        private double averageScore;
        private int totalGiftsGiven;
        private int totalGiftsReceived;
        private long totalAmountGiven;
        private long totalAmountReceived;
    }
}
