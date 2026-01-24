package com.relog.relog.gift.dto;

import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GiftResponse {

    private Long id;
    private String itemName;
    private Integer price;
    private LocalDate giftDate;
    private GiftType giftType;
    private GiftDirection direction;
    private String description;
    private Long friendId;
    private String friendName;

    public static GiftResponse from(Gift gift) {
        return GiftResponse.builder()
                .id(gift.getId())
                .itemName(gift.getItemName())
                .price(gift.getPrice())
                .giftDate(gift.getGiftDate())
                .giftType(gift.getGiftType())
                .direction(gift.getDirection())
                .description(gift.getDescription())
                .friendId(gift.getFriend().getId())
                .friendName(gift.getFriend().getName())
                .build();
    }
}
