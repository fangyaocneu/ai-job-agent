
package com.fangyao.agent.evaluation;

import com.fangyao.agent.AgentIntent;
import com.fangyao.agent.RoutingDecision;

public class EvaluationResult {

    private final String input;
    private final AgentIntent expected;
    private final RoutingDecision decision;

    public EvaluationResult(
            String input,
            AgentIntent expected,
            RoutingDecision decision
    ) {
        this.input = input;
        this.expected = expected;
        this.decision = decision;
    }

    public boolean isCorrect() {
        return expected == decision.intent();
    }

    public String getInput() {
        return input;
    }

    public AgentIntent getExpected() {
        return expected;
    }

    public AgentIntent getActual() {
        return decision.intent();
    }

    public long getLatencyMs() {
        return decision.latencyMs();
    }

    public RoutingDecision.RoutingSource getSource() {
        return decision.source();
    }

    public Double getConfidence() {
        return decision.confidence();
    }

    public String getFallbackReason() {
        return decision.fallbackReason();
    }
}
