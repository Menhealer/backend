package com.relog.relog.member.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {

    private String nickname;
    private LocalDate birthday;
    private LocalTime birthTime;
    private String profileImage;
}
