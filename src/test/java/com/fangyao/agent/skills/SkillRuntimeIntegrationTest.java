
package com.fangyao.agent.skills;

import com.fangyao.agent.AgentIntent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class SkillRuntimeIntegrationTest {

    @Test
    void resumeIntentShouldInjectResumeInstructions()
            throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        SkillSelector selector =
                new SkillSelector(registry);

        SkillDefinition skill = selector
                .selectAndLoad(AgentIntent.RESUME)
                .orElseThrow();

        SkillPromptComposer composer =
                new SkillPromptComposer();

        String prompt = composer.compose(
                "You are an AI career assistant.",
                skill.instructions()
        );

        assertTrue(prompt.contains(
                "Retrieve relevant resume chunks"
        ));

        assertTrue(prompt.contains(
                "Additional Selected Skill Instructions"
        ));
    }

    @Test
    void generalIntentShouldNotInjectAnySkill()
            throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        SkillSelector selector =
                new SkillSelector(registry);

        assertTrue(
                selector.selectAndLoad(
                        AgentIntent.GENERAL
                ).isEmpty()
        );

        SkillPromptComposer composer =
                new SkillPromptComposer();

        String originalPrompt =
                "You are an AI career assistant.";

        String prompt = composer.compose(
                originalPrompt,
                ""
        );

        assertEquals(originalPrompt, prompt);

        assertFalse(prompt.contains(
                "Additional Selected Skill Instructions"
        ));
    }
}
