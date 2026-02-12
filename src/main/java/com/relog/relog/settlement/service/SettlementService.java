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
import com.relog.relog.settlement.dto.QuarterlySettlementResponse.BestFriendResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse.FriendRankResponse;
import com.relog.relog.settlement.dto.QuarterlySettlementResponse.QuarterlySolutionResponse;
import com.relog.relog.settlement.repository.SettlementCacheRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        // 베스트 친구 (최대 5명)
        List<BestFriendResponse> bestFriends = findBestFriends(events, 5);
        // 워스트 친구 (최대 5명)
        List<FriendRankResponse> worstFriends = findWorstFriends(events, 5);

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
            List<BestFriendResponse> bestFriends, List<FriendRankResponse> worstFriends) {

        int startMonth = (quarter - 1) * 3 + 1;
        List<QuarterlyAiRequest.MonthlySummaryData> monthlySummaries = new ArrayList<>();

        for (int m = startMonth; m < startMonth + 3; m++) {
            int month = m;
            List<Event> monthEvents = events.stream()
                    .filter(e -> e.getEventDate().getMonthValue() == month)
                    .toList();

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

            monthlySummaries.add(QuarterlyAiRequest.MonthlySummaryData.builder()
                    .month(month)
                    .totalMeetings(monthEvents.size())
                    .positiveMeetings(positive)
                    .negativeMeetings(negative)
                    .averageScore(calculateAverage(totalScore, scoredCount))
                    .build());
        }

        List<QuarterlyAiRequest.FriendRankData> bestFriendData = bestFriends.stream()
                .map(f -> QuarterlyAiRequest.FriendRankData.builder()
                        .friendName(f.getFriend().getName())
                        .averageScore(f.getFriend().getScore()) // Friend score를 평균 점수 대신 사용하거나, 별도 계산 필요
                        .meetingCount(0) // BestFriendResponse에는 만남 횟수가 없으므로 0 혹은 별도 로직 필요
                        .positiveCount(0)
                        .negativeCount(0)
                        .build())
                .toList();

        List<QuarterlyAiRequest.FriendRankData> worstFriendData = worstFriends.stream()
                .map(f -> QuarterlyAiRequest.FriendRankData.builder()
                        .friendName(f.getFriendName())
                        .meetingCount(f.getMeetingCount())
                        .averageScore(f.getAverageScore())
                        .positiveCount(f.getPositiveCount())
                        .negativeCount(f.getNegativeCount())
                        .build())
                .toList();

        return QuarterlyAiRequest.builder()
                .year(year)
                .quarter(quarter)
                .monthlySummaries(monthlySummaries)
                .bestFriends(bestFriendData)
                .worstFriends(worstFriendData)
                .friendsToMaintain(List.of()) // 삭제된 필드는 빈 리스트로 처리
                .friendsNeedingAttention(List.of()) // 삭제된 필드는 빈 리스트로 처리
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
                continue;
            }
            receivedCount++;
            receivedAmount += price;
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
        Map.Entry<Long, List<Event>> topEntry = null;
        int maxCount = 0;

        for (Map.Entry<Long, List<Event>> entry : eventsByFriend.entrySet()) {
            if (entry.getValue().size() <= maxCount) {
                continue;
            }
            maxCount = entry.getValue().size();
            topEntry = entry;
        }

        return topEntry;
    }

    private List<BestFriendResponse> findBestFriends(List<Event> events, int limit) {
        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);

        return eventsByFriend.values().stream()
                .map(this::createBestFriend)
                .sorted(Comparator.comparingDouble((BestFriendResponse b) -> b.getFriend().getScore()).reversed())
                .limit(limit)
                .toList();
    }

    private BestFriendResponse createBestFriend(List<Event> events) {
        Friend friend = events.get(0).getFriend();
        return BestFriendResponse.builder()
                .friend(FriendResponse.from(friend))
                .recommendation("좋은 관계를 유지하고 있습니다.")
                .build();
    }

    private List<FriendRankResponse> findWorstFriends(List<Event> events, int limit) {
        Map<Long, List<Event>> eventsByFriend = groupEventsByFriend(events);

        return eventsByFriend.entrySet().stream()
                .map(entry -> createFriendRank(entry.getKey(), entry.getValue()))
                .filter(rank -> rank.getAverageScore() > 0)
                .sorted(Comparator.comparingDouble(FriendRankResponse::getAverageScore)
                        .thenComparingInt(FriendRankResponse::getNegativeCount).reversed())
                .limit(limit)
                .toList();
    }

    private Map<Long, List<Event>> groupEventsByFriend(List<Event> events) {
        Map<Long, List<Event>> result = new HashMap<>();

        for (Event event : events) {
            result.computeIfAbsent(event.getFriend().getId(), k -> new ArrayList<>()).add(event);
        }

        return result;
    }

    private FriendRankResponse createFriendRank(Long friendId, List<Event> events) {
        Friend friend = events.get(0).getFriend();
        int positiveCount = 0;
        int negativeCount = 0;

        for (Event event : events) {
            if (event.getReviewScore() == null) {
                continue;
            }
            int score = event.getReviewScore().getScore();
            positiveCount += countIfPositive(score);
            negativeCount += countIfNegative(score);
        }

        return FriendRankResponse.builder()
                .friendId(friendId)
                .friendName(friend.getName())
                .meetingCount(events.size())
                .averageScore(calculateAverageScore(events))
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .build();
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

        List<BestFriendResponse> bestFriends = List.of(
                BestFriendResponse.builder()
                        .friend(FriendResponse.from(friend1))
                        .recommendation("좋은 관계를 유지하고 있습니다.")
                        .build(),
                BestFriendResponse.builder()
                        .friend(FriendResponse.from(friend2))
                        .recommendation("좋은 관계를 유지하고 있습니다.")
                        .build());

        List<FriendRankResponse> worstFriends = List.of(
                FriendRankResponse.builder()
                        .friendId(4L).friendName("최예진")
                        .meetingCount(3).averageScore(2.0).positiveCount(0).negativeCount(2).build(),
                FriendRankResponse.builder()
                        .friendId(5L).friendName("정도윤")
                        .meetingCount(2).averageScore(2.5).positiveCount(0).negativeCount(1).build());

        QuarterlySolutionResponse solution = QuarterlySolutionResponse.builder()
                .overallAnalysis("이번 분기 총 37회의 만남이 있었으며 전반적으로 활발한 교류가 있었습니다.")
                .positiveInsights(List.of(
                        "김민수님과 매우 좋은 관계를 유지하고 있습니다.",
                        "이서연님과의 만남 만족도가 높습니다."))
                .negativeInsights(List.of(
                        "최예진님과의 관계 개선이 필요합니다."))
                .actionItems(List.of(
                        "최예진님과 솔직한 대화를 나눠보세요."))
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