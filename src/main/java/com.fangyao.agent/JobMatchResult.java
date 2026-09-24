package com.fangyao.agent;

public class JobMatchResult {

    private final int score;
    private final String reason;
    private final String gap;

    public JobMatchResult(
            int score,
            String reason,
            String gap
    ) {
        this.score = score;
        this.reason = reason;
        this.gap = gap;
    }

    public int getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }

    public String getGap() {
        return gap;
    }

    @Override
    public String toString() {
        return """
                Score: %d
                Reason: %s
                Gap: %s
                """.formatted(
                score,
                reason,
                gap
        );
    }
}