package com.relog.relog.member.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {

    private String nickname;
    private LocalDate birthday;
    private String profileImage;
}
