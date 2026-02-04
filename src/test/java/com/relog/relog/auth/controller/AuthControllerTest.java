package com.relog.relog.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.relog.relog.auth.dto.PasswordChangeRequest;
import com.relog.relog.auth.dto.SignUpRequest;
import com.relog.relog.auth.dto.TokenResponse;
import com.relog.relog.auth.exception.EmailAlreadyExistsException;
import com.relog.relog.auth.exception.InvalidCredentialsException;
import com.relog.relog.auth.service.AuthService;
import com.relog.relog.common.exception.GlobalExceptionHandler;
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
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(MEMBER_ID, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        given(authService.signUp(any(SignUpRequest.class))).willReturn(tokenResponse);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "Test1234!",
                                    "nickname": "테스터",
                                    "birthday": "1995-05-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_duplicateEmail() throws Exception {
        given(authService.signUp(any(SignUpRequest.class)))
                .willThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "existing@example.com",
                                    "password": "Test1234!",
                                    "nickname": "테스터"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증 (이메일 누락)")
    void signUp_validationFail() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "password": "Test1234!",
                                    "nickname": "테스터"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        given(authService.login(any())).willReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "Test1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 인증정보")
    void login_invalidCredentials() throws Exception {
        given(authService.login(any()))
                .willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_success() throws Exception {
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        given(authService.refresh(anyString())).willReturn(tokenResponse);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "old-refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        setAuthentication();
        willDoNothing().given(authService).logout(MEMBER_ID);

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 아님")
    void emailCheck_notDuplicate() throws Exception {
        given(authService.checkEmailDuplicate(anyString())).willReturn(false);

        mockMvc.perform(post("/auth/email-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "new@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.duplicate").value(false));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복")
    void emailCheck_duplicate() throws Exception {
        given(authService.checkEmailDuplicate(anyString())).willReturn(true);

        mockMvc.perform(post("/auth/email-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "existing@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicate").value(true));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        setAuthentication();
        willDoNothing().given(authService).changePassword(eq(MEMBER_ID), any(PasswordChangeRequest.class));

        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "currentPassword": "OldPass1!",
                                    "newPassword": "NewPass1!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
