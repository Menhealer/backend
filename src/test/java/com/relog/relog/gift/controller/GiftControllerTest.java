package com.relog.relog.gift.controller;

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
import com.relog.relog.gift.dto.GiftCreateRequest;
import com.relog.relog.gift.dto.GiftResponse;
import com.relog.relog.gift.dto.GiftUpdateRequest;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.entity.GiftType;
import com.relog.relog.gift.service.GiftService;
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
class GiftControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GiftService giftService;

    @InjectMocks
    private GiftController giftController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(giftController)
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

    private GiftResponse createGiftResponse() {
        return GiftResponse.builder()
                .id(1L)
                .price(50000)
                .giftDate(LocalDate.of(2025, 3, 10))
                .giftType(GiftType.BIRTHDAY)
                .direction(GiftDirection.GIVEN)
                .description("좋아하는 브랜드")
                .friendId(1L)
                .friendName("김친구")
                .build();
    }

    @Test
    @DisplayName("선물 등록")
    void createGift() throws Exception {
        given(giftService.createGift(eq(MEMBER_ID), any(GiftCreateRequest.class)))
                .willReturn(createGiftResponse());

        mockMvc.perform(post("/gifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "price": 50000,
                                    "giftDate": "2025-03-10",
                                    "giftType": "BIRTHDAY",
                                    "direction": "GIVEN",
                                    "description": "좋아하는 브랜드",
                                    "friendId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.price").value(50000))
                .andExpect(jsonPath("$.data.giftType").value("BIRTHDAY"))
                .andExpect(jsonPath("$.data.direction").value("GIVEN"));
    }

    @Test
    @DisplayName("전체 선물 목록 조회")
    void getAllGifts() throws Exception {
        given(giftService.getAllGifts(MEMBER_ID))
                .willReturn(List.of(createGiftResponse()));

        mockMvc.perform(get("/gifts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].description").value("좋아하는 브랜드"));
    }

    @Test
    @DisplayName("친구별 선물 조회")
    void getGiftsByFriend() throws Exception {
        given(giftService.getGiftsByFriend(MEMBER_ID, 1L))
                .willReturn(List.of(createGiftResponse()));

        mockMvc.perform(get("/gifts").param("friendId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("타입별 선물 조회")
    void getGiftsByType() throws Exception {
        given(giftService.getGiftsByType(MEMBER_ID, GiftType.BIRTHDAY))
                .willReturn(List.of(createGiftResponse()));

        mockMvc.perform(get("/gifts").param("type", "BIRTHDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("방향별 선물 조회")
    void getGiftsByDirection() throws Exception {
        given(giftService.getGiftsByDirection(MEMBER_ID, GiftDirection.GIVEN))
                .willReturn(List.of(createGiftResponse()));

        mockMvc.perform(get("/gifts").param("direction", "GIVEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("선물 수정")
    void updateGift() throws Exception {
        GiftResponse updatedResponse = GiftResponse.builder()
                .id(1L)
                .price(30000)
                .giftDate(LocalDate.of(2025, 3, 10))
                .giftType(GiftType.BIRTHDAY)
                .direction(GiftDirection.GIVEN)
                .friendId(1L)
                .friendName("김친구")
                .build();

        given(giftService.updateGift(eq(MEMBER_ID), eq(1L), any(GiftUpdateRequest.class)))
                .willReturn(updatedResponse);

        mockMvc.perform(put("/gifts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "price": 30000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(30000));
    }

    @Test
    @DisplayName("선물 삭제")
    void deleteGift() throws Exception {
        willDoNothing().given(giftService).deleteGift(MEMBER_ID, 1L);

        mockMvc.perform(delete("/gifts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
