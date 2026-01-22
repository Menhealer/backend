package com.relog.relog.friendgroup.dto;

import com.relog.relog.friendgroup.entity.FriendGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FriendGroupResponse {

    private Long id;
    private String name;

    public static FriendGroupResponse from(FriendGroup group) {
        return FriendGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .build();
    }
}
