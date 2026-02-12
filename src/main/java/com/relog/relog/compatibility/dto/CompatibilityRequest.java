package com.relog.relog.compatibility.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompatibilityRequest {

    @NotNull(message = "친구 ID는 필수입니다.")
    private Long friendId;
}
