package com.relog.relog.gift.dto;

import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GiftCreateRequest {

    private Integer price;

    @NotNull(message = "선물 날짜는 필수입니다.")
    private LocalDate giftDate;

    @NotNull(message = "선물 유형은 필수입니다.")
    private GiftType giftType;

    @NotNull(message = "선물 방향은 필수입니다.")
    private GiftDirection direction;

    private String description;

    @NotNull(message = "친구 ID는 필수입니다.")
    private Long friendId;
}
