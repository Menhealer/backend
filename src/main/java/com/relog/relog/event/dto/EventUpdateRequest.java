package com.relog.relog.event.dto;

import com.relog.relog.event.entity.ReviewScore;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventUpdateRequest {

    @Size(max = 12, message = "이벤트 제목은 12자 이하여야 합니다.")
    private String title;
    private LocalDate eventDate;
    private Long friendId;
    private ReviewScore reviewScore;
    @Size(max = 100, message = "리뷰는 100자 이하여야 합니다.")
    private String reviewText;
}
