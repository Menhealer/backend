package com.relog.relog.friend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class FriendCreateRequest {

    @NotBlank
    private String name;

    private LocalDate birthday;

    private Long groupId;
}
