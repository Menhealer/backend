package com.relog.relog.event.entity;

public enum ReviewScore {
    VERY_BAD(1),
    BAD(2),
    NEUTRAL(3),
    GOOD(4),
    VERY_GOOD(5);

    private final int score;

    ReviewScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
