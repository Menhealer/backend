package com.relog.relog.friendgroup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendGroupCreateRequest {

    @NotBlank(message = "단체 이름은 필수입니다.")
    private String name;
}
