package com.relog.relog.gift.dto;

import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class GiftCreateRequest {

    @NotBlank
    private String itemName;

    private Integer price;

    @NotNull
    private LocalDate giftDate;

    @NotNull
    private GiftType giftType;

    @NotNull
    private GiftDirection direction;

    @NotNull
    private Long friendId;
}
