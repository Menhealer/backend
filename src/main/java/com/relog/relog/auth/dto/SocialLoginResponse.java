package com.relog.relog.auth.dto;

import com.relog.relog.member.dto.MemberResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SocialLoginResponse {

    private boolean isNewMember;
    private String accessToken;
    private String refreshToken;
    private MemberResponse member;
}
