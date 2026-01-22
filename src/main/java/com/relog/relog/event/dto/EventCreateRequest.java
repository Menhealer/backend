package com.relog.relog.event.dto;

import com.relog.relog.event.entity.ReviewScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class EventCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    private Long friendId;

    private ReviewScore reviewScore;

    private String reviewText;
}
