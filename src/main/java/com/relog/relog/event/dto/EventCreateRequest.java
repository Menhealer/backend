package com.relog.relog.event.dto;

import com.relog.relog.event.entity.ReviewScore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EventCreateRequest {

    @NotBlank(message = "이벤트 제목은 필수입니다.")
    @Size(max = 12, message = "이벤트 제목은 12자 이하여야 합니다.")
    private String title;

    @NotNull(message = "이벤트 날짜는 필수입니다.")
    private LocalDate eventDate;

    @NotNull(message = "친구 ID는 필수입니다.")
    private Long friendId;

    private ReviewScore reviewScore;

    @Size(max = 100, message = "리뷰는 100자 이하여야 합니다.")
    private String reviewText;
}
