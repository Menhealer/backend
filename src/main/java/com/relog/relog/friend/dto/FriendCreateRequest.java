package com.relog.relog.friend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendCreateRequest {

    @NotBlank(message = "친구 이름은 필수입니다.")
    private String name;

    private LocalDate birthday;

    private Long groupId;
}
