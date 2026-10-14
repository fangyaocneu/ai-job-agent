
package com.fangyao.agent.evaluation;

import com.fangyao.agent.AgentIntent;
import com.fangyao.agent.JevClient;
import com.fangyao.agent.JevIntentRouter;
import com.fangyao.agent.RoutingDecision;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IntentGoldenDatasetTest {

    // Mock client ensures no real JEV API calls.
    private final JevClient mockClient = mock(JevClient.class);

    private final JevIntentRouter router =
            new JevIntentRouter(mockClient);

    @Test
    void allLocalGoldenCasesShouldPass() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<EvaluationCase> cases = mapper.readValue(
                new File("evaluation/intent_local_cases.json"),
                new TypeReference<List<EvaluationCase>>() {}
        );

        assertNotNull(cases);
        assertEquals(
                50,
                cases.size(),
                "Golden dataset should contain exactly 50 cases"
        );

        int localCount = 0;
        int fallbackCount = 0;

        for (EvaluationCase testCase : cases) {

            String input = testCase.getInput();
            AgentIntent expected = testCase.getExpectedIntent();

            assertNotNull(input);
            assertFalse(input.isBlank());
            assertNotNull(expected);

            RoutingDecision decision =
                    router.routeWithDetails(input);

            assertEquals(
                    expected,
                    decision.intent(),
                    "Incorrect intent for: " + input
            );

            if (expected != AgentIntent.GENERAL) {

                assertEquals(
                        RoutingDecision.RoutingSource.LOCAL,
                        decision.source(),
                        "Expected LOCAL for: " + input
                );

                assertNull(
                        decision.fallbackReason(),
                        "Unexpected fallback for: " + input
                );

                localCount++;

            } else {

                // The current dataset contains GENERAL inputs
                // that do not match local rules.
                // Without JEV, they must fall back safely.
                assertEquals(
                        RoutingDecision.RoutingSource.FALLBACK,
                        decision.source(),
                        "Expected FALLBACK for: " + input
                );

                assertEquals(
                        "JEV_API_KEY_MISSING",
                        decision.fallbackReason()
                );

                fallbackCount++;
            }
        }

        assertEquals(40, localCount);
        assertEquals(10, fallbackCount);

        // No external JEV calls should occur.
        verify(mockClient, never()).classifyIntent(anyString());

        System.out.println(
                "[QUALITY GATE] 50 Golden Cases Passed. "
                        + "LOCAL=" + localCount
                        + ", FALLBACK=" + fallbackCount
        );
    }
}
