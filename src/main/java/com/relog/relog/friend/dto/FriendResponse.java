package com.relog.relog.friend.dto;

import com.relog.relog.friend.entity.Friend;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FriendResponse {

    private Long id;
    private String name;
    private LocalDate birthday;
    private Long groupId;
    private String groupName;

    public static FriendResponse from(Friend friend) {
        return FriendResponse.builder()
                .id(friend.getId())
                .name(friend.getName())
                .birthday(friend.getBirthday())
                .groupId(friend.getFriendGroup() != null ? friend.getFriendGroup().getId() : null)
                .groupName(friend.getFriendGroup() != null ? friend.getFriendGroup().getName() : null)
                .build();
    }
}
