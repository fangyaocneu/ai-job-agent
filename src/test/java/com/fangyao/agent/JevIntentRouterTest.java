
package com.fangyao.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class JevIntentRouterTest {

    @Test
    void jdAnalysisShouldUseLocalJobMatchRoute() {

        JevIntentRouter router = new JevIntentRouter();

        RoutingDecision decision =
                router.routeWithDetails("Analyze JD requirements");

        assertEquals(
                AgentIntent.JOB_MATCH,
                decision.intent()
        );

        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                decision.source()
        );
    }

    @Test
    void resumeJobMatchingShouldUseJobMatchIntent() {

        JevIntentRouter router = new JevIntentRouter();

        RoutingDecision decision =
                router.routeWithDetails(
                        "Does my resume match this job?"
                );

        assertEquals(
                AgentIntent.JOB_MATCH,
                decision.intent()
        );

        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                decision.source()
        );
    }

    @Test
    void resumeAnalysisShouldRemainResumeIntent() {

        JevIntentRouter router = new JevIntentRouter();

        RoutingDecision decision =
                router.routeWithDetails("Analyze my resume");

        assertEquals(
                AgentIntent.RESUME,
                decision.intent()
        );

        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                decision.source()
        );
    }
}
