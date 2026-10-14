
package com.fangyao.agent;

public record RoutingDecision(
        AgentIntent intent,
        RoutingSource source,
        Double confidence,
        long latencyMs,
        String fallbackReason
) {
    public enum RoutingSource {
        LOCAL,
        JEV,
        FALLBACK
    }
}
