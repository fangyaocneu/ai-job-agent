package com.fangyao.agent.evaluation;

import com.fangyao.agent.AgentIntent;

public class EvaluationResult {

    private final String input;
    private final AgentIntent expected;
    private final AgentIntent actual;
    private final long latencyMs;

    public EvaluationResult(
            String input,
            AgentIntent expected,
            AgentIntent actual,
            long latencyMs
    ) {
        this.input = input;
        this.expected = expected;
        this.actual = actual;
        this.latencyMs = latencyMs;
    }

    public boolean isCorrect() {
        return expected == actual;
    }

    public String getInput() {
        return input;
    }

    public AgentIntent getExpected() {
        return expected;
    }

    public AgentIntent getActual() {
        return actual;
    }

    public long getLatencyMs() {
        return latencyMs;
    }
}