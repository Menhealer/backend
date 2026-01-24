package com.relog.relog.auth.service;

import com.relog.relog.auth.dto.LoginRequest;
import com.relog.relog.auth.dto.PasswordChangeRequest;
import com.relog.relog.auth.dto.SignUpRequest;
import com.relog.relog.auth.dto.TokenResponse;
import com.relog.relog.auth.exception.EmailAlreadyExistsException;
import com.relog.relog.auth.exception.InvalidCredentialsException;
import com.relog.relog.auth.exception.InvalidPasswordException;
import com.relog.relog.jwt.TokenGenerator;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final RelogMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        validateEmailNotExists(request.getEmail());

        RelogMember member = RelogMember.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .birthday(request.getBirthday())
                .build();

        RelogMember savedMember = memberRepository.save(member);

        return createTokenResponse(savedMember.getId());
    }

    public TokenResponse login(LoginRequest request) {
        RelogMember member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        validatePassword(request.getPassword(), member.getPassword());

        return createTokenResponse(member.getId());
    }

    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        RelogMember member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        validateCurrentPassword(request.getCurrentPassword(), member.getPassword());

        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private void validateEmailNotExists(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidCredentialsException();
        }
    }

    private void validateCurrentPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidPasswordException();
        }
    }

    private TokenResponse createTokenResponse(Long memberId) {
        return TokenResponse.builder()
                .accessToken(tokenGenerator.generateAccessToken(memberId))
                .refreshToken(tokenGenerator.generateRefreshToken(memberId))
                .build();
    }
}
