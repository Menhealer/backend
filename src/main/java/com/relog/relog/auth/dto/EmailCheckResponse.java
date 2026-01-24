package com.relog.relog.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailCheckResponse {

    private boolean duplicate;
}
