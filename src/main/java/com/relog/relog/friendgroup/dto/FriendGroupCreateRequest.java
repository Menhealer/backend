package com.relog.relog.friendgroup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FriendGroupCreateRequest {

    @NotBlank
    private String name;
}
