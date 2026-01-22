package com.relog.relog.friend.dto;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class FriendUpdateRequest {

    private String name;
    private LocalDate birthday;
    private Long groupId;
}
