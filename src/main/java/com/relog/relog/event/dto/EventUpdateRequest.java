package com.relog.relog.event.dto;

import com.relog.relog.event.entity.ReviewScore;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class EventUpdateRequest {

    private String title;
    private LocalDate eventDate;
    private Long friendId;
    private ReviewScore reviewScore;
    private String reviewText;
}
