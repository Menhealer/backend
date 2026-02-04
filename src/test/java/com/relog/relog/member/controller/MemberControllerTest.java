package com.relog.relog.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.relog.relog.common.exception.GlobalExceptionHandler;
import com.relog.relog.member.dto.MemberResponse;
import com.relog.relog.member.dto.MemberUpdateRequest;
import com.relog.relog.member.dto.ProfileImageResponse;
import com.relog.relog.member.service.MemberService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController)
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
    @DisplayName("내 정보 조회")
    void getMyInfo() throws Exception {
        MemberResponse response = MemberResponse.builder()
                .id(MEMBER_ID)
                .email("test@example.com")
                .nickname("테스터")
                .birthday(LocalDate.of(1995, 5, 15))
                .profileImage(null)
                .build();

        given(memberService.getMember(MEMBER_ID)).willReturn(response);

        mockMvc.perform(get("/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(MEMBER_ID))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    @DisplayName("내 정보 수정")
    void updateMyInfo() throws Exception {
        MemberResponse response = MemberResponse.builder()
                .id(MEMBER_ID)
                .email("test@example.com")
                .nickname("새닉네임")
                .birthday(LocalDate.of(1995, 6, 20))
                .build();

        given(memberService.updateMember(eq(MEMBER_ID), any(MemberUpdateRequest.class)))
                .willReturn(response);

        mockMvc.perform(put("/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nickname": "새닉네임",
                                    "birthday": "1995-06-20"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("프로필 이미지 업로드")
    void uploadProfileImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "image data".getBytes());

        ProfileImageResponse response = new ProfileImageResponse("https://storage.example.com/profile.jpg");

        given(memberService.uploadProfileImage(eq(MEMBER_ID), any())).willReturn(response);

        mockMvc.perform(multipart("/members/me/profile-image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://storage.example.com/profile.jpg"));
    }

    @Test
    @DisplayName("프로필 이미지 삭제")
    void deleteProfileImage() throws Exception {
        willDoNothing().given(memberService).deleteProfileImage(MEMBER_ID);

        mockMvc.perform(delete("/members/me/profile-image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deleteMyAccount() throws Exception {
        willDoNothing().given(memberService).deleteMember(MEMBER_ID);

        mockMvc.perform(delete("/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
