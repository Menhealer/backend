package com.relog.relog.event.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.relog.relog.common.exception.GlobalExceptionHandler;
import com.relog.relog.event.dto.CalendarMonthResponse;
import com.relog.relog.event.dto.EventCreateRequest;
import com.relog.relog.event.dto.EventResponse;
import com.relog.relog.event.dto.EventUpdateRequest;
import com.relog.relog.event.entity.ReviewScore;
import com.relog.relog.event.service.EventService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(MEMBER_ID, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private EventResponse createEventResponse() {
        return EventResponse.builder()
                .id(1L)
                .title("점심 약속")
                .eventDate(LocalDate.of(2025, 3, 15))
                .reviewScore(ReviewScore.GOOD)
                .reviewText("즐거웠다")
                .friendId(1L)
                .friendName("김친구")
                .build();
    }

    @Test
    @DisplayName("이벤트 생성")
    void createEvent() throws Exception {
        given(eventService.createEvent(eq(MEMBER_ID), any(EventCreateRequest.class)))
                .willReturn(createEventResponse());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "점심 약속",
                                    "eventDate": "2025-03-15",
                                    "friendId": 1,
                                    "reviewScore": "GOOD",
                                    "reviewText": "즐거웠다"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("점심 약속"))
                .andExpect(jsonPath("$.data.reviewScore").value("GOOD"))
                .andExpect(jsonPath("$.data.friendName").value("김친구"));
    }

    @Test
    @DisplayName("이벤트 단건 조회")
    void getEvent() throws Exception {
        given(eventService.getEvent(MEMBER_ID, 1L)).willReturn(createEventResponse());

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("점심 약속"));
    }

    @Test
    @DisplayName("캘린더 월별 조회")
    void getCalendarMonth() throws Exception {
        CalendarMonthResponse response = CalendarMonthResponse.builder()
                .year(2025)
                .month(3)
                .days(List.of())
                .build();

        given(eventService.getCalendarMonth(MEMBER_ID, 2025, 3)).willReturn(response);

        mockMvc.perform(get("/events/calendar")
                        .param("year", "2025")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(3));
    }

    @Test
    @DisplayName("이벤트 수정")
    void updateEvent() throws Exception {
        EventResponse updatedResponse = EventResponse.builder()
                .id(1L)
                .title("저녁 약속")
                .eventDate(LocalDate.of(2025, 3, 15))
                .reviewScore(ReviewScore.GOOD)
                .friendId(1L)
                .friendName("김친구")
                .build();

        given(eventService.updateEvent(eq(MEMBER_ID), eq(1L), any(EventUpdateRequest.class)))
                .willReturn(updatedResponse);

        mockMvc.perform(put("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "저녁 약속"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("저녁 약속"));
    }

    @Test
    @DisplayName("이벤트 삭제")
    void deleteEvent() throws Exception {
        willDoNothing().given(eventService).deleteEvent(MEMBER_ID, 1L);

        mockMvc.perform(delete("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
