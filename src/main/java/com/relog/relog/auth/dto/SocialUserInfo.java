package com.relog.relog.auth.dto;

import com.relog.relog.member.entity.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SocialUserInfo {

    private SocialProvider provider;
    private String providerId;
    private String email;
    private String nickname;
}
