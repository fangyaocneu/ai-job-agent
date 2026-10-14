
package com.fangyao.agent.evaluation;

import com.fangyao.agent.AgentIntent;
import com.fangyao.agent.JevIntentRouter;
import com.fangyao.agent.RoutingDecision;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JevIntentRouterRegressionTest {

    private final JevIntentRouter router =
            new JevIntentRouter();

    @Test
    void shouldRoutePreviouslyAppliedJobsToApplication() {

        RoutingDecision decision =
                router.routeWithDetails(
                        "Show me jobs I already applied to"
                );

        assertEquals(AgentIntent.APPLICATION, decision.intent());
        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                decision.source()
        );
    }

    @Test
    void shouldRouteJobSearchLocally() {

        RoutingDecision decision =
                router.routeWithDetails("Find backend jobs");

        assertEquals(AgentIntent.JOB_SEARCH, decision.intent());
        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                decision.source()
        );
    }

    @Test
    void shouldRouteJobMatchLocally() {

        RoutingDecision decision =
                router.routeWithDetails(
                        "Am I a good fit for this job? "
                        + "https://example.com/job"
                );

        assertEquals(AgentIntent.JOB_MATCH, decision.intent());
        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                decision.source()
        );
    }
}
