package com.relog.relog.friend.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FriendUpdateRequest {

    @Size(max = 10, message = "친구 이름은 10자 이하여야 합니다.")
    private String name;
    private LocalDate birthday;
    private String group;
}
