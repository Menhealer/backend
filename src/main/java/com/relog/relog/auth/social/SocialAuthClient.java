package com.relog.relog.auth.social;

import com.relog.relog.auth.dto.SocialUserInfo;

public interface SocialAuthClient {

    SocialUserInfo getUserInfo(String token);
}
