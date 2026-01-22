package com.relog.relog.friend.service;

import com.relog.relog.event.entity.Event;
import com.relog.relog.event.entity.ReviewScore;
import com.relog.relog.event.repository.EventRepository;
import com.relog.relog.friend.dto.FriendCreateRequest;
import com.relog.relog.friend.dto.FriendDetailResponse;
import com.relog.relog.friend.dto.FriendDetailResponse.EventSummaryResponse;
import com.relog.relog.friend.dto.FriendDetailResponse.GiftSummaryResponse;
import com.relog.relog.friend.dto.FriendDetailResponse.RelationshipScoreResponse;
import com.relog.relog.friend.dto.FriendResponse;
import com.relog.relog.friend.dto.FriendUpdateRequest;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.friend.exception.FriendNotFoundException;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.friendgroup.entity.FriendGroup;
import com.relog.relog.friendgroup.exception.FriendGroupNotFoundException;
import com.relog.relog.friendgroup.repository.FriendGroupRepository;
import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.repository.GiftRepository;
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
public class FriendService {

    private final FriendRepository friendRepository;
    private final RelogMemberRepository memberRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final EventRepository eventRepository;
    private final GiftRepository giftRepository;

    @Transactional
    public FriendResponse createFriend(Long memberId, FriendCreateRequest request) {
        RelogMember member = findMemberById(memberId);
        FriendGroup friendGroup = findFriendGroupIfExists(request.getGroupId(), memberId);

        Friend friend = Friend.builder()
                .name(request.getName())
                .birthday(request.getBirthday())
                .member(member)
                .friendGroup(friendGroup)
                .build();

        return FriendResponse.from(friendRepository.save(friend));
    }

    public List<FriendResponse> getAllFriends(Long memberId) {
        return friendRepository.findAllByMemberId(memberId).stream()
                .map(FriendResponse::from)
                .toList();
    }

    public List<FriendResponse> getFriendsByGroup(Long memberId, Long groupId) {
        return friendRepository.findAllByMemberIdAndFriendGroupId(memberId, groupId).stream()
                .map(FriendResponse::from)
                .toList();
    }

    public FriendDetailResponse getFriendDetail(Long memberId, Long friendId) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        List<Event> events = eventRepository.findAllByMemberIdAndFriendId(memberId, friendId);
        List<Gift> gifts = giftRepository.findAllByMemberIdAndFriendId(memberId, friendId);

        return FriendDetailResponse.builder()
                .friend(FriendResponse.from(friend))
                .relationshipScore(calculateRelationshipScore(events))
                .recentEvents(toEventSummaries(events))
                .giftHistory(toGiftSummaries(gifts))
                .build();
    }

    @Transactional
    public FriendResponse updateFriend(Long memberId, Long friendId, FriendUpdateRequest request) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);

        updateFriendName(friend, request.getName());
        updateFriendBirthday(friend, request.getBirthday());
        updateFriendGroup(friend, request.getGroupId(), memberId);

        return FriendResponse.from(friend);
    }

    @Transactional
    public void deleteFriend(Long memberId, Long friendId) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        friendRepository.delete(friend);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private Friend findFriendByIdAndMemberId(Long friendId, Long memberId) {
        return friendRepository.findByIdAndMemberId(friendId, memberId)
                .orElseThrow(FriendNotFoundException::new);
    }

    private FriendGroup findFriendGroupIfExists(Long groupId, Long memberId) {
        if (groupId == null) {
            return null;
        }
        return friendGroupRepository.findByIdAndMemberId(groupId, memberId)
                .orElseThrow(FriendGroupNotFoundException::new);
    }

    private void updateFriendName(Friend friend, String name) {
        if (name == null) {
            return;
        }
        friend.updateName(name);
    }

    private void updateFriendBirthday(Friend friend, java.time.LocalDate birthday) {
        if (birthday == null) {
            return;
        }
        friend.updateBirthday(birthday);
    }

    private void updateFriendGroup(Friend friend, Long groupId, Long memberId) {
        if (groupId == null) {
            return;
        }
        FriendGroup friendGroup = findFriendGroupIfExists(groupId, memberId);
        friend.updateFriendGroup(friendGroup);
    }

    private RelationshipScoreResponse calculateRelationshipScore(List<Event> events) {
        if (events.isEmpty()) {
            return createEmptyRelationshipScore();
        }

        int positiveCount = 0;
        int negativeCount = 0;
        int totalScore = 0;
        int scoredCount = 0;

        for (Event event : events) {
            if (event.getReviewScore() == null) {
                continue;
            }
            int score = event.getReviewScore().getScore();
            totalScore += score;
            scoredCount++;
            positiveCount += countPositive(score);
            negativeCount += countNegative(score);
        }

        double averageScore = calculateAverage(totalScore, scoredCount);

        return RelationshipScoreResponse.builder()
                .totalMeetings(events.size())
                .averageScore(averageScore)
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .build();
    }

    private RelationshipScoreResponse createEmptyRelationshipScore() {
        return RelationshipScoreResponse.builder()
                .totalMeetings(0)
                .averageScore(0.0)
                .positiveCount(0)
                .negativeCount(0)
                .build();
    }

    private int countPositive(int score) {
        if (score >= 4) {
            return 1;
        }
        return 0;
    }

    private int countNegative(int score) {
        if (score <= 2) {
            return 1;
        }
        return 0;
    }

    private double calculateAverage(int total, int count) {
        if (count == 0) {
            return 0.0;
        }
        return (double) total / count;
    }

    private List<EventSummaryResponse> toEventSummaries(List<Event> events) {
        return events.stream()
                .map(this::toEventSummary)
                .toList();
    }

    private EventSummaryResponse toEventSummary(Event event) {
        return EventSummaryResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate().toString())
                .reviewScore(getReviewScoreName(event.getReviewScore()))
                .build();
    }

    private String getReviewScoreName(ReviewScore reviewScore) {
        if (reviewScore == null) {
            return null;
        }
        return reviewScore.name();
    }

    private List<GiftSummaryResponse> toGiftSummaries(List<Gift> gifts) {
        return gifts.stream()
                .map(this::toGiftSummary)
                .toList();
    }

    private GiftSummaryResponse toGiftSummary(Gift gift) {
        return GiftSummaryResponse.builder()
                .giftId(gift.getId())
                .itemName(gift.getItemName())
                .price(gift.getPrice())
                .giftDate(gift.getGiftDate().toString())
                .giftType(gift.getGiftType().name())
                .direction(gift.getDirection().name())
                .build();
    }
}
