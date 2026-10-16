
package com.fangyao.agent.skills;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fangyao.agent.AgentIntent;

public class ChatRequestInputFactoryTest {

    private final ChatRequestInputFactory factory =
            new ChatRequestInputFactory();

    @Test
    void resumeShouldCreateRequestInput() throws Exception {

        SkillRuntime runtime = createRuntime();

        String prompt = runtime.buildPrompt(
                AgentIntent.RESUME,
                "Base career assistant prompt"
        );

        var prepared = factory.prepare(prompt);

        assertTrue(prepared.input().isMessage());
        assertTrue(prepared.prompt().contains("resume-analysis"));
        assertTrue(prepared.prompt().contains(
                "Retrieve relevant resume chunks"
        ));
    }

    @Test
    void jobMatchShouldCreateRequestInput() throws Exception {

        SkillRuntime runtime = createRuntime();

        String prompt = runtime.buildPrompt(
                AgentIntent.JOB_MATCH,
                "Base career assistant prompt"
        );

        var prepared = factory.prepare(prompt);

        assertTrue(prepared.input().isMessage());
        assertTrue(prepared.prompt().contains("job-matching"));
        assertFalse(prepared.prompt().contains(
                "Selected Agent Skill: resume-analysis"
        ));
    }

    @Test
    void generalShouldKeepOriginalPrompt() throws Exception {

        SkillRuntime runtime = createRuntime();

        String base = "Base career assistant prompt";

        var prepared = factory.prepare(
                runtime.buildPrompt(AgentIntent.GENERAL, base)
        );

        assertEquals(base, prepared.prompt());
        assertTrue(prepared.input().isMessage());
    }

    @Test
    void emptyPromptShouldFail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> factory.prepare("")
        );
    }

    private SkillRuntime createRuntime() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        return new SkillRuntime(
                new SkillSelector(registry)
        );
    }
}
