
package com.fangyao.agent.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fangyao.agent.AgentIntent;
import com.fangyao.agent.JevClient;
import com.fangyao.agent.JevIntentRouter;
import com.fangyao.agent.RoutingDecision;

public class JevIntentRouterMockTest {

    private JevClient mockClient;
    private JevIntentRouter router;

    // No local keyword rule should match this input.
    private static final String AMBIGUOUS_INPUT =
            "What opportunities would suit my background?";

    @BeforeEach
    void setUp() {
        mockClient = mock(JevClient.class);
        router = new JevIntentRouter(mockClient);
    }

    private void mockDecision(
            String intent,
            double confidence
    ) {
        JevClient.JevDecision decision =
                mock(JevClient.JevDecision.class);

        when(decision.getChoice()).thenReturn(intent);
        when(decision.getConfidence()).thenReturn(confidence);

        when(mockClient.isAvailable()).thenReturn(true);
        when(mockClient.classifyIntent(anyString()))
                .thenReturn(decision);
    }

    @Test
    void highConfidenceShouldUseJev() {

        mockDecision("JOB_SEARCH", 0.92);

        RoutingDecision result =
                router.routeWithDetails(AMBIGUOUS_INPUT);

        assertEquals(AgentIntent.JOB_SEARCH, result.intent());
        assertEquals(
                RoutingDecision.RoutingSource.JEV,
                result.source()
        );
        assertEquals(0.92, result.confidence(), 0.0001);
        assertNull(result.fallbackReason());

        verify(mockClient).classifyIntent(AMBIGUOUS_INPUT);
    }

    @Test
    void lowConfidenceShouldFallback() {

        mockDecision("JOB_SEARCH", 0.45);

        RoutingDecision result =
                router.routeWithDetails(AMBIGUOUS_INPUT);

        assertEquals(AgentIntent.GENERAL, result.intent());
        assertEquals(
                RoutingDecision.RoutingSource.FALLBACK,
                result.source()
        );
        assertEquals(
                "LOW_CONFIDENCE",
                result.fallbackReason()
        );
    }

    @Test
    void invalidConfidenceShouldFallback() {

        mockDecision("JOB_SEARCH", 1.5);

        RoutingDecision result =
                router.routeWithDetails(AMBIGUOUS_INPUT);

        assertEquals(AgentIntent.GENERAL, result.intent());
        assertEquals(
                "INVALID_CONFIDENCE",
                result.fallbackReason()
        );
    }

    @Test
    void unknownIntentShouldFallback() {

        mockDecision("UNKNOWN_CATEGORY", 0.95);

        RoutingDecision result =
                router.routeWithDetails(AMBIGUOUS_INPUT);

        assertEquals(AgentIntent.GENERAL, result.intent());
        assertEquals(
                "UNKNOWN_INTENT",
                result.fallbackReason()
        );
    }

    @Test
    void apiFailureShouldFallback() {

        when(mockClient.isAvailable()).thenReturn(true);

        when(mockClient.classifyIntent(anyString()))
                .thenThrow(
                        new RuntimeException("Simulated API failure")
                );

        RoutingDecision result =
                router.routeWithDetails(AMBIGUOUS_INPUT);

        assertEquals(AgentIntent.GENERAL, result.intent());
        assertEquals(
                RoutingDecision.RoutingSource.FALLBACK,
                result.source()
        );
        assertEquals(
                "JEV_REQUEST_FAILED",
                result.fallbackReason()
        );
    }

    @Test
    void localRouteShouldSkipJev() {

        RoutingDecision result =
                router.routeWithDetails("Find Java jobs");

        assertEquals(AgentIntent.JOB_SEARCH, result.intent());
        assertEquals(
                RoutingDecision.RoutingSource.LOCAL,
                result.source()
        );

        // Ensure zero calls to the JEV client.
        verifyNoInteractions(mockClient);
    }
}
