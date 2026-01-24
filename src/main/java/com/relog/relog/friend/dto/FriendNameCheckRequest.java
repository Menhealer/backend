package com.relog.relog.friend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendNameCheckRequest {

    @NotBlank(message = "친구 이름은 필수입니다.")
    private String name;
}
