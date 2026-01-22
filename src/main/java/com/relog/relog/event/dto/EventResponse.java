package com.relog.relog.event.dto;

import com.relog.relog.event.entity.Event;
import com.relog.relog.event.entity.ReviewScore;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private LocalDate eventDate;
    private ReviewScore reviewScore;
    private String reviewText;
    private Long friendId;
    private String friendName;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate())
                .reviewScore(event.getReviewScore())
                .reviewText(event.getReviewText())
                .friendId(event.getFriend().getId())
                .friendName(event.getFriend().getName())
                .build();
    }
}
