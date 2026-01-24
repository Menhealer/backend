package com.relog.relog.member.dto;

import com.relog.relog.member.entity.RelogMember;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberResponse {

    private Long id;
    private String email;
    private String nickname;
    private LocalDate birthday;
    private String profileImage;

    public static MemberResponse from(RelogMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .profileImage(member.getProfileImage())
                .build();
    }
}
