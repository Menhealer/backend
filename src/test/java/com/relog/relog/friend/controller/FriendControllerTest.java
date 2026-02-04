package com.relog.relog.friend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.relog.relog.friend.dto.FriendCreateRequest;
import com.relog.relog.friend.dto.FriendDetailResponse;
import com.relog.relog.friend.dto.FriendDetailResponse.RelationshipScoreResponse;
import com.relog.relog.friend.dto.FriendResponse;
import com.relog.relog.friend.dto.FriendUpdateRequest;
import com.relog.relog.friend.service.FriendService;
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
class FriendControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FriendService friendService;

    @InjectMocks
    private FriendController friendController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(friendController)
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

    private FriendResponse createFriendResponse(Long id, String name) {
        return FriendResponse.builder()
                .id(id)
                .name(name)
                .birthday(LocalDate.of(1995, 3, 10))
                .groupId(null)
                .groupName(null)
                .build();
    }

    @Test
    @DisplayName("친구 생성")
    void createFriend() throws Exception {
        given(friendService.createFriend(eq(MEMBER_ID), any(FriendCreateRequest.class)))
                .willReturn(createFriendResponse(1L, "김친구"));

        mockMvc.perform(post("/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "김친구",
                                    "birthday": "1995-03-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("김친구"));
    }

    @Test
    @DisplayName("전체 친구 목록 조회")
    void getAllFriends() throws Exception {
        given(friendService.getAllFriends(MEMBER_ID))
                .willReturn(List.of(
                        createFriendResponse(1L, "김친구"),
                        createFriendResponse(2L, "이친구")));

        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("김친구"));
    }

    @Test
    @DisplayName("그룹별 친구 목록 조회")
    void getFriendsByGroup() throws Exception {
        given(friendService.getFriendsByGroup(MEMBER_ID, 1L))
                .willReturn(List.of(createFriendResponse(1L, "김친구")));

        mockMvc.perform(get("/friends").param("groupId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("친구 상세 조회")
    void getFriendDetail() throws Exception {
        FriendDetailResponse detailResponse = FriendDetailResponse.builder()
                .friend(createFriendResponse(1L, "김친구"))
                .relationshipScore(RelationshipScoreResponse.builder()
                        .totalMeetings(5)
                        .averageScore(4.2)
                        .positiveCount(4)
                        .negativeCount(0)
                        .build())
                .recentEvents(List.of())
                .giftHistory(List.of())
                .build();

        given(friendService.getFriendDetail(MEMBER_ID, 1L)).willReturn(detailResponse);

        mockMvc.perform(get("/friends/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.friend.name").value("김친구"))
                .andExpect(jsonPath("$.data.relationshipScore.totalMeetings").value(5))
                .andExpect(jsonPath("$.data.relationshipScore.averageScore").value(4.2));
    }

    @Test
    @DisplayName("친구 이름 중복 확인")
    void checkName() throws Exception {
        given(friendService.isNameDuplicate(eq(MEMBER_ID), anyString())).willReturn(false);

        mockMvc.perform(post("/friends/name-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "김친구"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicate").value(false));
    }

    @Test
    @DisplayName("친구 정보 수정")
    void updateFriend() throws Exception {
        FriendResponse updatedResponse = FriendResponse.builder()
                .id(1L)
                .name("김새친구")
                .birthday(LocalDate.of(1995, 3, 10))
                .build();

        given(friendService.updateFriend(eq(MEMBER_ID), eq(1L), any(FriendUpdateRequest.class)))
                .willReturn(updatedResponse);

        mockMvc.perform(put("/friends/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "김새친구"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("김새친구"));
    }

    @Test
    @DisplayName("친구 삭제")
    void deleteFriend() throws Exception {
        willDoNothing().given(friendService).deleteFriend(MEMBER_ID, 1L);

        mockMvc.perform(delete("/friends/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
