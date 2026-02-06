package com.relog.relog.friend.service;

import com.relog.relog.event.entity.Event;
import com.relog.relog.gift.entity.Gift;
import com.relog.relog.gift.entity.GiftDirection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Re Log 관계 점수 로직 (v1.0)
 *
 * 최종 점수 S = A + B + C (범위: -100 ~ +100)
 *
 * A: 이벤트 별점 (-80 ~ +80)
 * B: 이벤트 빈도 (0 ~ +10)
 * C: 선물 점수 (-10 ~ +10)
 */
@Component
public class RelationshipScoreCalculator {

    // === A: 이벤트 별점 상수 ===
    private static final int RATING_CENTER = 3;
    private static final int RATING_MULTIPLIER = 8;
    private static final double RECENCY_DECAY_DAYS = 30.0;
    private static final double EVENT_RATING_MIN = -80.0;
    private static final double EVENT_RATING_MAX = 80.0;

    // === B: 이벤트 빈도 상수 ===
    private static final int FREQUENCY_WINDOW_DAYS = 90;
    private static final int FREQUENCY_CAP = 5;
    private static final int FREQUENCY_MULTIPLIER = 2;

    // === C: 선물 점수 상수 ===
    private static final double GIFT_BASE_SCALE = 4.5;
    private static final double GIFT_BASE_MAX = 10.0;
    private static final double COUNT_IMBALANCE_WEIGHT = 1.4;
    private static final double AMOUNT_IMBALANCE_WEIGHT = 0.35;
    private static final double GIFT_SCORE_MIN = -10.0;
    private static final double GIFT_SCORE_MAX = 10.0;

    // === 총점 범위 ===
    private static final int TOTAL_MIN = -100;
    private static final int TOTAL_MAX = 100;

    /**
     * 관계 점수 계산: S = A + B + C
     */
    public int calculate(List<Event> events, List<Gift> gifts) {
        double a = calculateEventRatingScore(events);
        double b = calculateEventFrequencyScore(events);
        double c = calculateGiftScore(gifts);

        int total = (int) Math.round(a + b + c);
        return clampInt(total, TOTAL_MIN, TOTAL_MAX);
    }

    /**
     * A: 이벤트 별점 (-80 ~ +80)
     *
     * 별점 1~5에서 3을 빼서 중심 맞추고 ×8
     * 1점 → -16 / 2점 → -8 / 3점 → 0 / 4점 → +8 / 5점 → +16
     *
     * 최근성 가중치: e^(-days/30)
     * 30일 → 37%, 90일 → 5%
     *
     * 모든 이벤트 합산 후 -80~+80 클램프
     */
    double calculateEventRatingScore(List<Event> events) {
        LocalDate today = LocalDate.now();
        double weightedSum = 0.0;

        for (Event event : events) {
            if (event.getReviewScore() == null) {
                continue;
            }

            double centered = (event.getReviewScore().getScore() - RATING_CENTER) * RATING_MULTIPLIER;

            long daysSince = ChronoUnit.DAYS.between(event.getEventDate(), today);
            if (daysSince < 0) {
                daysSince = 0;
            }

            double weight = Math.exp(-daysSince / RECENCY_DECAY_DAYS);
            weightedSum += centered * weight;
        }

        return clamp(weightedSum, EVENT_RATING_MIN, EVENT_RATING_MAX);
    }

    /**
     * B: 이벤트 빈도 (0 ~ +10)
     *
     * 최근 90일 만남 횟수 기반
     * 0회=0 / 1회=2 / 2회=4 / 3회=6 / 4회=8 / 5회+=10
     * 안 만나도 감점 없음
     */
    double calculateEventFrequencyScore(List<Event> events) {
        LocalDate cutoff = LocalDate.now().minusDays(FREQUENCY_WINDOW_DAYS);

        long recentCount = events.stream()
                .filter(e -> !e.getEventDate().isBefore(cutoff))
                .count();

        int capped = (int) Math.min(recentCount, FREQUENCY_CAP);
        return capped * FREQUENCY_MULTIPLIER;
    }

    /**
     * C: 선물 점수 (-10 ~ +10)
     *
     * 기본 점수: min(sqrt(총횟수) × 4.5, 10)
     * 균형 보정:
     *   - 횟수 균형: 한쪽만 계속 주면 감점 (가중치 1.4)
     *   - 금액 균형: 금액 차이 나면 감점 (가중치 0.35)
     *
     * 예시:
     *   서로 3번씩, 비슷한 금액 → +9.8
     *   서로 3번씩, 5만원 vs 5천원 → +7.1
     *   나만 5번, 금액도 한쪽 → -7.3
     *   선물 없음 → 0
     */
    double calculateGiftScore(List<Gift> gifts) {
        if (gifts.isEmpty()) {
            return 0.0;
        }

        int givenCount = 0;
        int receivedCount = 0;
        long givenAmount = 0;
        long receivedAmount = 0;

        for (Gift gift : gifts) {
            if (gift.getDirection() == GiftDirection.GIVEN) {
                givenCount++;
                givenAmount += gift.getPrice() != null ? gift.getPrice() : 0;
            } else {
                receivedCount++;
                receivedAmount += gift.getPrice() != null ? gift.getPrice() : 0;
            }
        }

        int totalCount = givenCount + receivedCount;
        double base = Math.min(Math.sqrt(totalCount) * GIFT_BASE_SCALE, GIFT_BASE_MAX);

        // 횟수 균형: 0 (완전 편향) ~ 1 (완벽 균형)
        double countImbalance = (double) Math.abs(givenCount - receivedCount) / totalCount;

        // 금액 균형: 0 (완전 편향) ~ 1 (완벽 균형)
        long totalAmount = givenAmount + receivedAmount;
        double amountImbalance = (totalAmount > 0)
                ? (double) Math.abs(givenAmount - receivedAmount) / totalAmount
                : 0.0;

        // 패널티: 횟수 불균형 × 1.4 + 금액 불균형 × 0.35
        double penalty = countImbalance * COUNT_IMBALANCE_WEIGHT
                + amountImbalance * AMOUNT_IMBALANCE_WEIGHT;

        double score = base * (1.0 - penalty);
        return clamp(score, GIFT_SCORE_MIN, GIFT_SCORE_MAX);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
