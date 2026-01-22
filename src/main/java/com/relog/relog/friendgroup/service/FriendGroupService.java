package com.relog.relog.friendgroup.service;

import com.relog.relog.friendgroup.dto.FriendGroupCreateRequest;
import com.relog.relog.friendgroup.dto.FriendGroupResponse;
import com.relog.relog.friendgroup.entity.FriendGroup;
import com.relog.relog.friendgroup.exception.FriendGroupNameDuplicateException;
import com.relog.relog.friendgroup.exception.FriendGroupNotFoundException;
import com.relog.relog.friendgroup.repository.FriendGroupRepository;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendGroupService {

    private final FriendGroupRepository friendGroupRepository;
    private final RelogMemberRepository memberRepository;

    @Transactional
    public FriendGroupResponse createGroup(Long memberId, FriendGroupCreateRequest request) {
        RelogMember member = findMemberById(memberId);
        validateGroupNameNotDuplicate(request.getName(), memberId);

        FriendGroup group = FriendGroup.builder()
                .name(request.getName())
                .member(member)
                .build();

        return FriendGroupResponse.from(friendGroupRepository.save(group));
    }

    public List<FriendGroupResponse> getAllGroups(Long memberId) {
        return friendGroupRepository.findAllByMemberId(memberId).stream()
                .map(FriendGroupResponse::from)
                .toList();
    }

    @Transactional
    public FriendGroupResponse updateGroup(Long memberId, Long groupId, FriendGroupCreateRequest request) {
        FriendGroup group = findGroupByIdAndMemberId(groupId, memberId);
        validateGroupNameNotDuplicate(request.getName(), memberId);

        group.updateName(request.getName());
        return FriendGroupResponse.from(group);
    }

    @Transactional
    public void deleteGroup(Long memberId, Long groupId) {
        FriendGroup group = findGroupByIdAndMemberId(groupId, memberId);
        friendGroupRepository.delete(group);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private FriendGroup findGroupByIdAndMemberId(Long groupId, Long memberId) {
        return friendGroupRepository.findByIdAndMemberId(groupId, memberId)
                .orElseThrow(FriendGroupNotFoundException::new);
    }

    private void validateGroupNameNotDuplicate(String name, Long memberId) {
        if (friendGroupRepository.existsByNameAndMemberId(name, memberId)) {
            throw new FriendGroupNameDuplicateException();
        }
    }
}
