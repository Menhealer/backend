package com.relog.relog.member.dto;

import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.entity.SocialProvider;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberResponse {

    private Long id;
    private String nickname;
    private LocalDate birthday;
    private LocalTime birthTime;
    private String profileImage;
    private SocialProvider provider;

    public static MemberResponse from(RelogMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .birthTime(member.getBirthTime())
                .profileImage(member.getProfileImage())
                .provider(member.getProvider())
                .build();
    }
}
