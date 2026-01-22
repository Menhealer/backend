package com.relog.relog.member.dto;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class MemberUpdateRequest {

    private String nickname;
    private LocalDate birthday;
}
