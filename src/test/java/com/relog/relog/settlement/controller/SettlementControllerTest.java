package com.relog.relog.settlement.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.relog.relog.common.exception.GlobalExceptionHandler;
import com.relog.relog.settlement.dto.MonthlySettlementResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse.RelationshipSolutionResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse.SummaryResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse.TopFriendResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse.QuarterlySolutionResponse;
import com.relog.relog.settlement.service.SettlementService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SettlementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SettlementService settlementService;

    @InjectMocks
    private SettlementController settlementController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(settlementController)
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

    @Test
    @DisplayName("월간 정산 조회")
    void getMonthlySettlement() throws Exception {
        MonthlySettlementResponse response = MonthlySettlementResponse.builder()
                .year(2025)
                .month(3)
                .summary(SummaryResponse.builder()
                        .totalMeetings(10)
                        .positiveMeetings(7)
                        .negativeMeetings(1)
                        .averageScore(3.8)
                        .totalGiftsGiven(3)
                        .totalGiftsReceived(2)
                        .totalAmountGiven(150000)
                        .totalAmountReceived(100000)
                        .build())
                .topFriend(TopFriendResponse.builder()
                        .friendId(1L)
                        .friendName("김친구")
                        .meetingCount(5)
                        .averageScore(4.2)
                        .build())
                .solution(RelationshipSolutionResponse.builder()
                        .friendName("김친구")
                        .analysis("좋은 관계를 유지하고 있습니다.")
                        .suggestions(List.of("정기적인 만남을 유지하세요."))
                        .build())
                .build();

        given(settlementService.getMonthlySettlement(MEMBER_ID, 2025, 3)).willReturn(response);

        mockMvc.perform(get("/settlements/monthly")
                        .param("year", "2025")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(3))
                .andExpect(jsonPath("$.data.summary.totalMeetings").value(10))
                .andExpect(jsonPath("$.data.topFriend.friendName").value("김친구"))
                .andExpect(jsonPath("$.data.solution.analysis").value("좋은 관계를 유지하고 있습니다."));
    }

    @Test
    @DisplayName("분기 정산 조회")
    void getQuarterlySettlement() throws Exception {
        QuarterlySettlementResponse response = QuarterlySettlementResponse.builder()
                .year(2025)
                .quarter(1)
                .bestFriends(List.of())
                .worstFriends(List.of())
                .friendsToMaintain(List.of())
                .friendsNeedingAttention(List.of())
                .solution(QuarterlySolutionResponse.builder()
                        .overallAnalysis("1분기 관계 분석 결과입니다.")
                        .positiveInsights(List.of("긍정적 만남이 많습니다."))
                        .negativeInsights(List.of())
                        .actionItems(List.of("오래 연락하지 못한 친구에게 연락하세요."))
                        .build())
                .build();

        given(settlementService.getQuarterlySettlement(MEMBER_ID, 2025, 1)).willReturn(response);

        mockMvc.perform(get("/settlements/quarterly")
                        .param("year", "2025")
                        .param("quarter", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.quarter").value(1))
                .andExpect(jsonPath("$.data.solution.overallAnalysis").value("1분기 관계 분석 결과입니다."));
    }
}
