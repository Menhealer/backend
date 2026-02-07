package com.relog.relog.friend.service;

import com.relog.relog.event.entity.Event;
import com.relog.relog.event.entity.ReviewScore;
import com.relog.relog.event.repository.EventRepository;
import com.relog.relog.friend.dto.FriendCreateRequest;
import com.relog.relog.friend.dto.FriendDetailResponse;
import com.relog.relog.friend.dto.FriendDetailResponse.EventSummaryResponse;
import com.relog.relog.friend.dto.FriendDetailResponse.GiftSummaryResponse;
import com.relog.relog.friend.dto.FriendResponse;
import com.relog.relog.friend.dto.FriendUpdateRequest;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.friend.exception.FriendNameDuplicateException;
import com.relog.relog.friend.exception.FriendNotFoundException;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.repository.GiftRepository;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import java.time.LocalDate;
import java.util.Comparator;
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
    private final EventRepository eventRepository;
    private final GiftRepository giftRepository;
    private final RelationshipScoreCalculator scoreCalculator;

    @Transactional
    public FriendResponse createFriend(Long memberId, FriendCreateRequest request) {
        RelogMember member = findMemberById(memberId);
        validateFriendNameNotDuplicate(memberId, request.getName());

        Friend friend = Friend.builder()
                .name(request.getName())
                .birthday(request.getBirthday())
                .member(member)
                .group(request.getGroup())
                .build();

        return FriendResponse.from(friendRepository.save(friend));
    }

    public List<FriendResponse> getAllFriends(Long memberId) {
        return friendRepository.findAllByMemberIdOrderByName(memberId).stream()
                .map(FriendResponse::from)
                .toList();
    }

    public FriendDetailResponse getFriendDetail(Long memberId, Long friendId) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        List<Event> events = eventRepository.findAllWithFriendByMemberIdAndFriendId(memberId, friendId);
        List<Gift> gifts = giftRepository.findAllWithFriendByMemberIdAndFriendId(memberId, friendId);

        return FriendDetailResponse.builder()
                .friend(FriendResponse.from(friend))
                .score(scoreCalculator.calculate(events, gifts))
                .recentEvents(toRecentEventSummaries(events))
                .giftHistory(toRecentGiftSummaries(gifts))
                .build();
    }

    public boolean isNameDuplicate(Long memberId, String name) {
        return friendRepository.existsByMemberIdAndName(memberId, name);
    }

    @Transactional
    public FriendResponse updateFriend(Long memberId, Long friendId, FriendUpdateRequest request) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);

        updateFriendName(friend, request.getName(), memberId);
        updateFriendBirthday(friend, request.getBirthday());
        updateFriendGroup(friend, request.getGroup());

        return FriendResponse.from(friend);
    }

    @Transactional
    public void deleteFriend(Long memberId, Long friendId) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        friendRepository.delete(friend);
    }

    @Transactional
    public void recalculateScore(Long memberId, Long friendId) {
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        List<Event> events = eventRepository.findAllWithFriendByMemberIdAndFriendId(memberId, friendId);
        List<Gift> gifts = giftRepository.findAllWithFriendByMemberIdAndFriendId(memberId, friendId);

        int score = scoreCalculator.calculate(events, gifts);
        friend.updateScore(score);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private Friend findFriendByIdAndMemberId(Long friendId, Long memberId) {
        return friendRepository.findByIdAndMemberId(friendId, memberId)
                .orElseThrow(FriendNotFoundException::new);
    }

    private void validateFriendNameNotDuplicate(Long memberId, String name) {
        if (friendRepository.existsByMemberIdAndName(memberId, name)) {
            throw new FriendNameDuplicateException();
        }
    }

    private void updateFriendName(Friend friend, String name, Long memberId) {
        if (name == null) {
            return;
        }
        if (name.equals(friend.getName())) {
            return;
        }
        validateFriendNameNotDuplicate(memberId, name);
        friend.updateName(name);
    }

    private void updateFriendBirthday(Friend friend, LocalDate birthday) {
        if (birthday == null) {
            return;
        }
        friend.updateBirthday(birthday);
    }

    private void updateFriendGroup(Friend friend, String group) {
        if (group == null) {
            return;
        }
        friend.updateGroup(group);
    }

    private List<EventSummaryResponse> toRecentEventSummaries(List<Event> events) {
        return events.stream()
                .sorted(Comparator.comparing(Event::getEventDate).reversed())
                .limit(3)
                .map(this::toEventSummary)
                .toList();
    }

    private EventSummaryResponse toEventSummary(Event event) {
        return EventSummaryResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate().toString())
                .reviewScore(getReviewScoreName(event.getReviewScore()))
                .reviewText(event.getReviewText())
                .build();
    }

    private String getReviewScoreName(ReviewScore reviewScore) {
        if (reviewScore == null) {
            return null;
        }
        return reviewScore.name();
    }

    private List<GiftSummaryResponse> toRecentGiftSummaries(List<Gift> gifts) {
        return gifts.stream()
                .sorted(Comparator.comparing(Gift::getGiftDate).reversed())
                .limit(3)
                .map(this::toGiftSummary)
                .toList();
    }

    private GiftSummaryResponse toGiftSummary(Gift gift) {
        return GiftSummaryResponse.builder()
                .giftId(gift.getId())
                .price(gift.getPrice())
                .giftDate(gift.getGiftDate().toString())
                .giftType(gift.getGiftType().name())
                .direction(gift.getDirection().name())
                .description(gift.getDescription())
                .build();
    }
}
