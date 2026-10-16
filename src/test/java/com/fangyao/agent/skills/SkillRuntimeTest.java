
package com.fangyao.agent.skills;

import com.fangyao.agent.AgentIntent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SkillRuntimeTest {

    @Test
    void resumeShouldLoadCorrectInstructions() throws Exception {
        SkillRuntime runtime = createRuntime();

        String prompt = runtime.buildPrompt(
                AgentIntent.RESUME,
                "Base career assistant prompt"
        );

        assertTrue(prompt.contains("resume-analysis"));
        assertTrue(prompt.contains(
                "Retrieve relevant resume chunks"
        ));
        assertFalse(prompt.contains(
                "Calculate a weighted matching score"
        ));
    }

    @Test
    void jobMatchShouldLoadCorrectInstructions() throws Exception {
        SkillRuntime runtime = createRuntime();

        String prompt = runtime.buildPrompt(
                AgentIntent.JOB_MATCH,
                "Base career assistant prompt"
        );

        assertTrue(prompt.contains("job-matching"));
        assertTrue(prompt.contains(
                "Calculate a weighted matching score"
        ));
        assertFalse(prompt.contains(
                "Retrieve relevant resume chunks"
        ));
    }

    @Test
    void generalShouldNotLoadSkill() throws Exception {
        SkillRuntime runtime = createRuntime();

        String basePrompt = "Base career assistant prompt";

        String prompt = runtime.buildPrompt(
                AgentIntent.GENERAL,
                basePrompt
        );

        assertEquals(basePrompt, prompt);
    }

    private SkillRuntime createRuntime() throws Exception {
        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        return new SkillRuntime(
                new SkillSelector(registry)
        );
    }
}
