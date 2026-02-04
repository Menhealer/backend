package com.relog.relog.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialSignUpRequest {

    @NotBlank(message = "provider는 필수입니다.")
    private String provider;

    @NotBlank(message = "토큰은 필수입니다.")
    private String token;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    private LocalDate birthday;
}
