package com.relog.relog.event.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CalendarMonthResponse {

    private int year;
    private int month;
    private List<CalendarDayResponse> days;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class CalendarDayResponse {
        private LocalDate date;
        private List<EventResponse> events;
        private List<BirthdayResponse> birthdays;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BirthdayResponse {
        private Long friendId;
        private String friendName;
        private boolean isMemberBirthday;
    }
}
