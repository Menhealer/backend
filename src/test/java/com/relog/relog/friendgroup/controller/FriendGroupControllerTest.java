package com.relog.relog.friendgroup.controller;

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
import com.relog.relog.friendgroup.dto.FriendGroupCreateRequest;
import com.relog.relog.friendgroup.dto.FriendGroupResponse;
import com.relog.relog.friendgroup.service.FriendGroupService;
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
class FriendGroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FriendGroupService friendGroupService;

    @InjectMocks
    private FriendGroupController friendGroupController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(friendGroupController)
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
    @DisplayName("그룹 생성")
    void createGroup() throws Exception {
        FriendGroupResponse response = FriendGroupResponse.builder()
                .id(1L)
                .name("대학친구")
                .build();

        given(friendGroupService.createGroup(eq(MEMBER_ID), any(FriendGroupCreateRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "대학친구"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("대학친구"));
    }

    @Test
    @DisplayName("전체 그룹 조회")
    void getAllGroups() throws Exception {
        given(friendGroupService.getAllGroups(MEMBER_ID))
                .willReturn(List.of(
                        FriendGroupResponse.builder().id(1L).name("대학친구").build(),
                        FriendGroupResponse.builder().id(2L).name("회사동료").build()));

        mockMvc.perform(get("/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("대학친구"))
                .andExpect(jsonPath("$.data[1].name").value("회사동료"));
    }

    @Test
    @DisplayName("그룹 수정")
    void updateGroup() throws Exception {
        FriendGroupResponse response = FriendGroupResponse.builder()
                .id(1L)
                .name("고등학교친구")
                .build();

        given(friendGroupService.updateGroup(eq(MEMBER_ID), eq(1L), any(FriendGroupCreateRequest.class)))
                .willReturn(response);

        mockMvc.perform(put("/groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "고등학교친구"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("고등학교친구"));
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() throws Exception {
        willDoNothing().given(friendGroupService).deleteGroup(MEMBER_ID, 1L);

        mockMvc.perform(delete("/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
