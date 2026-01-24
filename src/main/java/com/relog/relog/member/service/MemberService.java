package com.relog.relog.member.service;

import com.relog.relog.member.dto.MemberResponse;
import com.relog.relog.member.dto.MemberUpdateRequest;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final RelogMemberRepository memberRepository;

    public MemberResponse getMember(Long memberId) {
        RelogMember member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        RelogMember member = findMemberById(memberId);

        updateNickname(member, request.getNickname());
        updateBirthday(member, request.getBirthday());
        updateProfileImage(member, request.getProfileImage());

        return MemberResponse.from(member);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        RelogMember member = findMemberById(memberId);
        memberRepository.delete(member);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void updateNickname(RelogMember member, String nickname) {
        if (nickname == null) {
            return;
        }
        member.updateNickname(nickname);
    }

    private void updateBirthday(RelogMember member, java.time.LocalDate birthday) {
        if (birthday == null) {
            return;
        }
        member.updateBirthday(birthday);
    }

    private void updateProfileImage(RelogMember member, String profileImage) {
        if (profileImage == null) {
            return;
        }
        member.updateProfileImage(profileImage);
    }
}
