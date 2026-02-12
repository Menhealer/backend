package com.relog.relog.compatibility.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompatibilityResponse {

    private String memberName;
    private String friendName;
    private LocalDate memberBirthday;
    private LocalDate friendBirthday;
    private String summary;
    private int overallScore;
    private ProfileInfo memberProfile;
    private ProfileInfo friendProfile;
    private String analysis;
    private ElementBalance elementBalance;
    private List<CategoryResult> categories;
    private String advice;
    private String luckyItem;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProfileInfo {
        private String name;
        private String zodiacAnimal;
        private String zodiacSign;
        private String element;
        private String sajuPillar;
        private String personality;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ElementBalance {
        private String description;
        private String strongElement;
        private String weakElement;
        private String interaction;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryResult {
        private String category;
        private String emoji;
        private int score;
        private String description;
    }
}
