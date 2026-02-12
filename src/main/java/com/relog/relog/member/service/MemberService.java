package com.relog.relog.member.service;

import com.relog.relog.member.dto.MemberResponse;
import com.relog.relog.member.dto.MemberUpdateRequest;
import com.relog.relog.member.dto.ProfileImageResponse;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import com.relog.relog.storage.OciStorageService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final RelogMemberRepository memberRepository;
    private final Optional<OciStorageService> ociStorageService;

    public MemberResponse getMember(Long memberId) {
        RelogMember member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        RelogMember member = findMemberById(memberId);

        updateNickname(member, request.getNickname());
        updateBirthday(member, request.getBirthday());
        updateBirthTime(member, request.getBirthTime());

        return MemberResponse.from(member);
    }

    @Transactional
    public ProfileImageResponse uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        RelogMember member = findMemberById(memberId);

        deleteExistingProfileImage(member);

        String imageUrl = ociStorageService
                .orElseThrow(() -> new UnsupportedOperationException("파일 업로드 서비스가 비활성화 상태입니다."))
                .uploadProfileImage(memberId, file);
        member.updateProfileImage(imageUrl);

        return new ProfileImageResponse(imageUrl);
    }

    @Transactional
    public void deleteProfileImage(Long memberId) {
        RelogMember member = findMemberById(memberId);

        deleteExistingProfileImage(member);
        member.updateProfileImage(null);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void deleteExistingProfileImage(RelogMember member) {
        if (member.getProfileImage() == null) {
            return;
        }
        ociStorageService.ifPresent(service -> service.deleteProfileImage(member.getProfileImage()));
    }

    private void updateNickname(RelogMember member, String nickname) {
        if (nickname == null) {
            return;
        }
        member.updateNickname(nickname);
    }

    private void updateBirthday(RelogMember member, LocalDate birthday) {
        if (birthday == null) {
            return;
        }
        member.updateBirthday(birthday);
    }

    private void updateBirthTime(RelogMember member, LocalTime birthTime) {
        if (birthTime == null) {
            return;
        }
        member.updateBirthTime(birthTime);
    }
}
