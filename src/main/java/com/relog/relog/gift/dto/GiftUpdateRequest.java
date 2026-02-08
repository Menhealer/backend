package com.relog.relog.gift.dto;

import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GiftUpdateRequest {

    private Long friendId;
    private Integer price;
    private LocalDate giftDate;
    private GiftType giftType;
    private GiftDirection direction;
    private String description;
}
