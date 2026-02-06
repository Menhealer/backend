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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final RelogMemberRepository memberRepository;
    private final OciStorageService ociStorageService;

    public MemberResponse getMember(Long memberId) {
        RelogMember member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        RelogMember member = findMemberById(memberId);

        updateNickname(member, request.getNickname());
        updateBirthday(member, request.getBirthday());

        return MemberResponse.from(member);
    }

    @Transactional
    public ProfileImageResponse uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        RelogMember member = findMemberById(memberId);

        deleteExistingProfileImage(member);

        String imageUrl = ociStorageService.uploadProfileImage(memberId, file);
        member.updateProfileImage(imageUrl);

        return new ProfileImageResponse(imageUrl);
    }

    @Transactional
    public void deleteProfileImage(Long memberId) {
        RelogMember member = findMemberById(memberId);

        deleteExistingProfileImage(member);
        member.updateProfileImage(null);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        RelogMember member = findMemberById(memberId);

        deleteExistingProfileImage(member);
        memberRepository.delete(member);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void deleteExistingProfileImage(RelogMember member) {
        if (member.getProfileImage() == null) {
            return;
        }
        ociStorageService.deleteProfileImage(member.getProfileImage());
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
}
