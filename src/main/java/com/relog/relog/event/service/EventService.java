package com.relog.relog.event.service;

import com.relog.relog.event.dto.CalendarMonthResponse;
import com.relog.relog.event.dto.CalendarMonthResponse.BirthdayResponse;
import com.relog.relog.event.dto.CalendarMonthResponse.CalendarDayResponse;
import com.relog.relog.event.dto.EventCreateRequest;
import com.relog.relog.event.dto.EventResponse;
import com.relog.relog.event.dto.EventUpdateRequest;
import com.relog.relog.event.entity.Event;
import com.relog.relog.event.exception.EventNotFoundException;
import com.relog.relog.event.repository.EventRepository;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.friend.exception.FriendNotFoundException;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.exception.MemberNotFoundException;
import com.relog.relog.member.repository.RelogMemberRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final RelogMemberRepository memberRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public EventResponse createEvent(Long memberId, EventCreateRequest request) {
        RelogMember member = findMemberById(memberId);
        Friend friend = findFriendByIdAndMemberId(request.getFriendId(), memberId);

        Event event = Event.builder()
                .title(request.getTitle())
                .eventDate(request.getEventDate())
                .reviewScore(request.getReviewScore())
                .reviewText(request.getReviewText())
                .member(member)
                .friend(friend)
                .build();

        return EventResponse.from(eventRepository.save(event));
    }

    public List<EventResponse> getEventsByDate(Long memberId, LocalDate date) {
        return eventRepository.findAllByMemberIdAndEventDate(memberId, date).stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getEvent(Long memberId, Long eventId) {
        Event event = findEventByIdAndMemberId(eventId, memberId);
        return EventResponse.from(event);
    }

    public CalendarMonthResponse getCalendarMonth(Long memberId, int year, int month) {
        RelogMember member = findMemberById(memberId);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Event> events = eventRepository.findAllByMemberIdAndEventDateBetween(memberId, startDate, endDate);
        List<Friend> friends = friendRepository.findAllByMemberId(memberId);
        Map<LocalDate, List<Event>> eventsByDate = groupEventsByDate(events);

        List<CalendarDayResponse> days = buildCalendarDays(yearMonth, eventsByDate, member, friends);

        return CalendarMonthResponse.builder()
                .year(year)
                .month(month)
                .days(days)
                .build();
    }

    @Transactional
    public EventResponse updateEvent(Long memberId, Long eventId, EventUpdateRequest request) {
        Event event = findEventByIdAndMemberId(eventId, memberId);

        updateEventTitle(event, request.getTitle());
        updateEventDate(event, request.getEventDate());
        updateEventFriend(event, request.getFriendId(), memberId);
        updateEventReview(event, request);

        return EventResponse.from(event);
    }

    @Transactional
    public void deleteEvent(Long memberId, Long eventId) {
        Event event = findEventByIdAndMemberId(eventId, memberId);
        eventRepository.delete(event);
    }

    private RelogMember findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private Friend findFriendByIdAndMemberId(Long friendId, Long memberId) {
        return friendRepository.findByIdAndMemberId(friendId, memberId)
                .orElseThrow(FriendNotFoundException::new);
    }

    private Event findEventByIdAndMemberId(Long eventId, Long memberId) {
        return eventRepository.findByIdAndMemberId(eventId, memberId)
                .orElseThrow(EventNotFoundException::new);
    }

    private Map<LocalDate, List<Event>> groupEventsByDate(List<Event> events) {
        return events.stream()
                .collect(Collectors.groupingBy(Event::getEventDate));
    }

    private List<CalendarDayResponse> buildCalendarDays(
            YearMonth yearMonth,
            Map<LocalDate, List<Event>> eventsByDate,
            RelogMember member,
            List<Friend> friends) {
        
        List<CalendarDayResponse> days = new ArrayList<>();
        
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<EventResponse> dayEvents = getDayEvents(eventsByDate, date);
            List<BirthdayResponse> birthdays = getBirthdaysForDate(member, friends, date);

            days.add(CalendarDayResponse.builder()
                    .date(date)
                    .events(dayEvents)
                    .birthdays(birthdays)
                    .build());
        }
        
        return days;
    }

    private List<EventResponse> getDayEvents(Map<LocalDate, List<Event>> eventsByDate, LocalDate date) {
        return eventsByDate.getOrDefault(date, List.of()).stream()
                .map(EventResponse::from)
                .toList();
    }

    private List<BirthdayResponse> getBirthdaysForDate(RelogMember member, List<Friend> friends, LocalDate date) {
        List<BirthdayResponse> birthdays = new ArrayList<>();

        addMemberBirthdayIfMatch(birthdays, member, date);
        addFriendBirthdaysIfMatch(birthdays, friends, date);

        return birthdays;
    }

    private void addMemberBirthdayIfMatch(List<BirthdayResponse> birthdays, RelogMember member, LocalDate date) {
        if (!isBirthdayMatch(member.getBirthday(), date)) {
            return;
        }
        birthdays.add(BirthdayResponse.builder()
                .friendId(null)
                .friendName(member.getNickname())
                .isMemberBirthday(true)
                .build());
    }

    private void addFriendBirthdaysIfMatch(List<BirthdayResponse> birthdays, List<Friend> friends, LocalDate date) {
        for (Friend friend : friends) {
            if (!isBirthdayMatch(friend.getBirthday(), date)) {
                continue;
            }
            birthdays.add(BirthdayResponse.builder()
                    .friendId(friend.getId())
                    .friendName(friend.getName())
                    .isMemberBirthday(false)
                    .build());
        }
    }

    private boolean isBirthdayMatch(LocalDate birthday, LocalDate date) {
        if (birthday == null) {
            return false;
        }
        return birthday.getMonth() == date.getMonth() && birthday.getDayOfMonth() == date.getDayOfMonth();
    }

    private void updateEventTitle(Event event, String title) {
        if (title == null) {
            return;
        }
        event.updateTitle(title);
    }

    private void updateEventDate(Event event, LocalDate eventDate) {
        if (eventDate == null) {
            return;
        }
        event.updateEventDate(eventDate);
    }

    private void updateEventFriend(Event event, Long friendId, Long memberId) {
        if (friendId == null) {
            return;
        }
        Friend friend = findFriendByIdAndMemberId(friendId, memberId);
        event.updateFriend(friend);
    }

    private void updateEventReview(Event event, EventUpdateRequest request) {
        if (request.getReviewScore() == null && request.getReviewText() == null) {
            return;
        }
        event.updateReview(request.getReviewScore(), request.getReviewText());
    }
}
