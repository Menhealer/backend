package com.relog.relog.settlement.service;

import com.relog.relog.ai.AiAnalysisService;
import com.relog.relog.ai.dto.MonthlyAiRequest;
import com.relog.relog.ai.dto.MonthlyAnalysisResult;
import com.relog.relog.ai.dto.QuarterlyAiRequest;
import com.relog.relog.ai.dto.QuarterlyAnalysisResult;
import com.relog.relog.event.entity.Event;
import com.relog.relog.event.repository.EventRepository;
import com.relog.relog.friend.dto.FriendResponse;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.friend.repository.FriendRepository;
import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import com.relog.relog.gift.repository.GiftRepository;
import com.relog.relog.settlement.dto.MonthlySettlementResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse.RelationshipSolutionResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse.SummaryResponse;
import com.relog.relog.settlement.dto.MonthlySettlementResponse.TopFriendResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse.AnalyzedFriendResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse.QuarterlySolutionResponse;
import com.relog.relog.settlement.repository.SettlementCacheRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final EventRepository eventRepository;
    private final GiftRepository giftRepository;
    private final FriendRepository friendRepository;
    private final AiAnalysisService aiAnalysisService;
    private final SettlementCacheRepository settlementCacheRepository;

    @Value("${settlement.mock-enabled:false}")
    private boolean mockEnabled;

    private static final int POSITIVE_THRESHOLD = 4;
    private static final int NEGATIVE_THRESHOLD = 2;

    public MonthlySettlementResponse getMonthlySettlement(Long memberId, int year, int month) {
        if (mockEnabled) {
            return createMockMonthlyResponse(year, month);
        }

        Optional<MonthlySettlementResponse> cached = settlementCacheRepository.findMonthly(memberId, year, month);
        if (cached.isPresent()) {
            return cached.get();
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Event> events = eventRepository.findAllWithFriendByMemberIdAndDateRange(memberId, startDate, endDate);
        List<Gift> gifts = giftRepository.findAllWithFriendByMemberIdAndDateRange(memberId, startDate, endDate);

        TopFriendResponse topFriend = findTopFriend(events);
        SummaryResponse summary = calculateSummary(events, gifts);

        MonthlyAiRequest aiRequest = buildMonthlyAiRequest(topFriend, events, gifts, summary);
        MonthlyAnalysisResult aiResult = aiAnalysisService.analyzeMonthly(aiRequest);

        MonthlySettlementResponse response = MonthlySettlementResponse.builder()
                .year(year)
                .month(month)
                .summary(summary)
                .topFriend(topFriend)
                .solution(toRelationshipSolution(aiResult))
                .build();

        settlementCacheRepository.saveMonthly(memberId, year, month, response);

        return response;
    }

    public QuarterlySettlementResponse getQuarterlySettlement(Long memberId, int year, int quarter) {
        if (mockEnabled) {
            return createMockQuarterlyResponse(year, quarter);
        }

        Optional<QuarterlySettlementResponse> cached = settlementCacheRepository.findQuarterly(memberId, year, quarter);
        if (cached.isPresent()) {
            return cached.get();
        }

        LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);

        List<Event> events = eventRepository.findAllWithFriendByMemberIdAndDateRange(memberId, startDate, endDate);

        List<AnalyzedFriendResponse> bestFriends = findBestFriends(events, 5);
        List<AnalyzedFriendResponse> worstFriends = findWorstFriends(events, 5);

        QuarterlyAiRequest aiRequest = buildQuarterlyAiRequest(year, quarter, events, bestFriends, worstFriends);
        QuarterlyAnalysisResult aiResult = aiAnalysisService.analyzeQuarterly(aiRequest);

        QuarterlySettlementResponse response = QuarterlySettlementResponse.builder()
                .year(year)
                .quarter(quarter)
                .bestFriends(bestFriends)
                .worstFriends(worstFriends)
                .solution(toQuarterlySolution(aiResult))
                .build();

        settlementCacheRepository.saveQuarterly(memberId, year, quarter, response);

        return response;
    }

    private MonthlyAiRequest buildMonthlyAiRequest(
            TopFriendResponse topFriend, List<Event> events, List<Gift> gifts, SummaryResponse summary) {

        MonthlyAiRequest.SummaryData summaryData = MonthlyAiRequest.SummaryData.builder()
                .totalMeetings(summary.getTotalMeetings())
                .positiveMeetings(summary.getPositiveMeetings())
                .negativeMeetings(summary.getNegativeMeetings())
                .averageScore(summary.getAverageScore())
                .totalGiftsGiven(summary.getTotalGiftsGiven())
                .totalGiftsReceived(summary.getTotalGiftsReceived())
                .totalAmountGiven(summary.getTotalAmountGiven())
                .totalAmountReceived(summary.getTotalAmountReceived())
                .build();

        if (topFriend == null) {
            return MonthlyAiRequest.builder()
                    .friendName(null)
                    .events(List.of())
                    .gifts(List.of())
                    .summary(summaryData)
                    .build();
        }

        Long topFriendId = topFriend.getFriendId();

        List<MonthlyAiRequest.EventData> eventDataList = events.stream()
                .filter(e -> e.getFriend().getId().equals(topFriendId))
                .map(e -> MonthlyAiRequest.EventData.builder()
                        .eventDate(e.getEventDate())
                        .reviewScore(e.getReviewScore() != null ? e.getReviewScore().getScore() : 0)
                        .reviewText(e.getReviewText())
                        .build())
                .toList();

        List<MonthlyAiRequest.GiftData> giftDataList = gifts.stream()
                .filter(g -> g.getFriend().getId().equals(topFriendId))
                .map(g -> MonthlyAiRequest.GiftData.builder()
                        .giftDate(g.getGiftDate())
                        .giftType(g.getGiftType().name())
                        .direction(g.getDirection().name())
                        .price(g.getPrice())
                        .description(g.getDescription())
                        .build())
                .toList();

        return MonthlyAiRequest.builder()
                .friendName(topFriend.getFriendName())
                .events(eventDataList)
                .gifts(giftDataList)
                .summary(summaryData)
                .build();
    }

    private QuarterlyAiRequest buildQuarterlyAiRequest(
            int year, int quarter, List<Event> events,
            List<AnalyzedFriendResponse> bestFriends, List<AnalyzedFriendResponse> worstFriends) {

        int startMonth = (quarter - 1) * 3 + 1;
        List<QuarterlyAiRequest.MonthlySummaryData> monthlySummaries = new ArrayList<>();

        for (int m = startMonth; m < startMonth + 3; m++) {
            List<Event> monthEvents = filterEventsByMonth(events, m);
            monthlySummaries.add(calculateMonthlySummary(m, monthEvents));
        }

        List<QuarterlyAiRequest.FriendRankData> bestFriendData = bestFriends.stream()
                .map(f -> mapToFriendRankData(f.getFriend()))
                .toList();

        List<QuarterlyAiRequest.FriendRankData> worstFriendData = worstFriends.stream()
                .map(f -> mapToFriendRankData(f.getFriend()))
                .toList();

        return QuarterlyAiRequest.builder()
                .year(year)
                .quarter(quarter)
                .monthlySummaries(monthlySummaries)
                .bestFriends(bestFriendData)
                .worstFriends(worstFriendData)
                .friendsToMaintain(List.of())
                .friendsNeedingAttention(List.of())
                .build();
    }

    private List<Event> filterEventsByMonth(List<Event> events, int month) {
        return events.stream()
                .filter(e -> e.getEventDate().getMonthValue() == month)
                .toList();
    }

    private QuarterlyAiRequest.MonthlySummaryData calculateMonthlySummary(int month, List<Event> monthEvents) {
        int positive = 0;
        int negative = 0;
        int totalScore = 0;
        int scoredCount = 0;

        for (Event event : monthEvents) {
            if (event.getReviewScore() == null) {
                continue;
            }
            int score = event.getReviewScore().getScore();
            totalScore += score;
            scoredCount++;
            positive += countIfPositive(score);
            negative += countIfNegative(score);
        }

        return QuarterlyAiRequest.MonthlySummaryData.builder()
                .month(month)
                .totalMeetings(monthEvents.size())
                .positiveMeetings(positive)
                .negativeMeetings(negative)
                .averageScore(calculateAverage(totalScore, scoredCount))
                .build();
    }

    private QuarterlyAiRequest.FriendRankData mapToFriendRankData(FriendResponse friend) {
        return QuarterlyAiRequest.FriendRankData.builder()
                .friendName(friend.getName())
                .averageScore(friend.getScore())
                .meetingCount(0)
                .positiveCount(0)
                .negativeCount(0)
                .build();
    }

    private RelationshipSolutionResponse toRelationshipSolution(MonthlyAnalysisResult result) {
        return RelationshipSolutionResponse.builder()
                .friendName(result.getFriendName())
                .analysis(result.getAnalysis())
                .suggestions(result.getSuggestions())
                .build();
    }

    private QuarterlySolutionResponse toQuarterlySolution(QuarterlyAnalysisResult result) {
        return QuarterlySolutionResponse.builder()
                .overallAnalysis(result.getOverallAnalysis())
                .positiveInsights(result.getPositiveInsights())
                .negativeInsights(result.getNegativeInsights())
                .actionItems(result.getActionItems())
                .build();
    }

    private SummaryResponse calculateSummary(List<Event> events, List<Gift> gifts) {
        int positiveMeetings = 0;
        int negativeMeetings = 0;
        int totalScore = 0;
        int scoredCount = 0;

        for (Event event : events) {
            if (event.getReviewScore() == null) {
                continue;
            }
            int score = event.getReviewScore().getScore();
            totalScore += score;
            scoredCount++;
            positiveMeetings += countIfPositive(score);
            negativeMeetings += countIfNegative(score);
        }

        GiftSummary giftSummary = calculateGiftSummary(gifts);

        return SummaryResponse.builder()
                .totalMeetings(events.size())
                .positiveMeetings(positiveMeetings)
                .negativeMeetings(negativeMeetings)
                .averageScore(calculateAverage(totalScore, scoredCount))
                .totalGiftsGiven(giftSummary.givenCount)
                .totalGiftsReceived(giftSummary.receivedCount)
                .totalAmountGiven(giftSummary.givenAmount)
                .totalAmountReceived(giftSummary.receivedAmount)
                .build();
    }

    private GiftSummary calculateGiftSummary(List<Gift> gifts) {
        int givenCount = 0;
        int receivedCount = 0;
        long givenAmount = 0;
        long receivedAmount = 0;

        for (Gift gift : gifts) {
            int price = gift.getPrice() != null ? gift.getPrice() : 0;
            if (gift.getDirection() == GiftDirection.GIVEN) {
                givenCount++;
                givenAmount += price;
            } else {
                receivedCount++;
                receivedAmount += price;
            }
        }

        return new GiftSummary(givenCount, receivedCount, givenAmount, receivedAmount);
    }

    private TopFriendResponse findTopFriend(List<Event> events) {
        if (events.isEmpty()) {
            return null;
        }

        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);
        Map.Entry<Long, List<Event>> topEntry = findTopEntry(eventsByFriend);

        if (topEntry == null) {
            return null;
        }

        List<Event> topFriendEvents = topEntry.getValue();
        Friend topFriend = topFriendEvents.get(0).getFriend();

        return TopFriendResponse.builder()
                .friendId(topEntry.getKey())
                .friendName(topFriend.getName())
                .meetingCount(topFriendEvents.size())
                .averageScore(calculateAverageScore(topFriendEvents))
                .build();
    }

    private Map.Entry<Long, List<Event>> findTopEntry(Map<Long, List<Event>> eventsByFriend) {
        return eventsByFriend.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .orElse(null);
    }

    private List<AnalyzedFriendResponse> findBestFriends(List<Event> events, int limit) {
        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);

        return eventsByFriend.values().stream()
                .filter(e -> !e.isEmpty())
                .sorted(Comparator.comparingDouble((List<Event> e) -> e.get(0).getFriend().getScore()).reversed())
                .limit(limit)
                .map(this::createBestFriend)
                .toList();
    }

    private AnalyzedFriendResponse createBestFriend(List<Event> events) {
        Friend friend = events.get(0).getFriend();
        return AnalyzedFriendResponse.builder()
                .friend(FriendResponse.from(friend))
                .recommendation("좋은 관계를 유지하고 있습니다.")
                .build();
    }

    private List<AnalyzedFriendResponse> findWorstFriends(List<Event> events, int limit) {
        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);

        return eventsByFriend.values().stream()
                .filter(this::hasScore)
                .sorted(Comparator.comparingDouble(this::calculateAverageScore))
                .limit(limit)
                .map(this::createWorstFriend)
                .toList();
    }

    private boolean hasScore(List<Event> events) {
        return calculateAverageScore(events) > 0;
    }

    private AnalyzedFriendResponse createWorstFriend(List<Event> events) {
        Friend friend = events.get(0).getFriend();
        return AnalyzedFriendResponse.builder()
                .friend(FriendResponse.from(friend))
                .recommendation("관계 개선이 필요합니다.")
                .build();
    }

    private Map<Long, List<Event>> groupEventsByFriend(List<Event> events) {
        return events.stream()
                .collect(Collectors.groupingBy(e -> e.getFriend().getId()));
    }

    private double calculateAverageScore(List<Event> events) {
        int totalScore = 0;
        int count = 0;

        for (Event event : events) {
            if (event.getReviewScore() == null) {
                continue;
            }
            totalScore += event.getReviewScore().getScore();
            count++;
        }

        return calculateAverage(totalScore, count);
    }

    private int countIfPositive(int score) {
        if (score >= POSITIVE_THRESHOLD) {
            return 1;
        }
        return 0;
    }

    private int countIfNegative(int score) {
        if (score <= NEGATIVE_THRESHOLD) {
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

    private MonthlySettlementResponse createMockMonthlyResponse(int year, int month) {
        SummaryResponse summary = SummaryResponse.builder()
                .totalMeetings(12)
                .positiveMeetings(8)
                .negativeMeetings(2)
                .averageScore(3.8)
                .totalGiftsGiven(3)
                .totalGiftsReceived(2)
                .totalAmountGiven(150000)
                .totalAmountReceived(80000)
                .build();

        TopFriendResponse topFriend = TopFriendResponse.builder()
                .friendId(1L)
                .friendName("김민수")
                .meetingCount(5)
                .averageScore(4.2)
                .build();

        RelationshipSolutionResponse solution = RelationshipSolutionResponse.builder()
                .friendName("김민수")
                .analysis("김민수님과의 관계가 매우 좋습니다. 이번 달 5회 만남을 가졌으며 평균 만족도가 높습니다.")
                .suggestions(List.of(
                        "이 관계를 계속 유지하세요.",
                        "특별한 날에 선물을 준비해보세요.",
                        "함께 새로운 활동을 시도해보세요."))
                .build();

        return MonthlySettlementResponse.builder()
                .year(year)
                .month(month)
                .summary(summary)
                .topFriend(topFriend)
                .solution(solution)
                .build();
    }

    private QuarterlySettlementResponse createMockQuarterlyResponse(int year, int quarter) {
        Friend friend1 = Friend.builder().id(1L).name("김민수").score(85).build();
        Friend friend2 = Friend.builder().id(2L).name("이서연").score(70).build();
        Friend friend3 = Friend.builder().id(3L).name("최예진").score(30).build();
        Friend friend4 = Friend.builder().id(4L).name("정도윤").score(40).build();

        List<AnalyzedFriendResponse> bestFriends = List.of(
                AnalyzedFriendResponse.builder()
                        .friend(FriendResponse.from(friend1))
                        .recommendation("좋은 관계를 유지하고 있습니다.")
                        .build(),
                AnalyzedFriendResponse.builder()
                        .friend(FriendResponse.from(friend2))
                        .recommendation("좋은 관계를 유지하고 있습니다.")
                        .build());

        List<AnalyzedFriendResponse> worstFriends = List.of(
                AnalyzedFriendResponse.builder()
                        .friend(FriendResponse.from(friend3))
                        .recommendation("관계 개선이 필요합니다.")
                        .build(),
                AnalyzedFriendResponse.builder()
                        .friend(FriendResponse.from(friend4))
                        .recommendation("관계 개선이 필요합니다.")
                        .build());

        QuarterlySolutionResponse solution = QuarterlySolutionResponse.builder()
                .overallAnalysis("Mock Analysis")
                .positiveInsights(List.of("Good"))
                .negativeInsights(List.of("Bad"))
                .actionItems(List.of("Action"))
                .build();

        return QuarterlySettlementResponse.builder()
                .year(year)
                .quarter(quarter)
                .bestFriends(bestFriends)
                .worstFriends(worstFriends)
                .solution(solution)
                .build();
    }

    private record GiftSummary(int givenCount, int receivedCount, long givenAmount, long receivedAmount) {}
}