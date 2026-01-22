package com.relog.relog.gift.dto;

import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class GiftUpdateRequest {

    private String itemName;
    private Integer price;
    private LocalDate giftDate;
    private GiftType giftType;
    private GiftDirection direction;
}
