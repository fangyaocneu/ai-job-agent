
package com.fangyao.agent.skills;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fangyao.agent.AgentIntent;

public class TaskAwareSkillRuntimeTest {

    private final TaskSelector taskSelector =
            new TaskSelector();

    private SkillRuntime createRuntime() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        return new SkillRuntime(
                new SkillSelector(registry)
        );
    }

    @Test
    void jdAnalysisShouldLoadOnlyJdSkill() throws Exception {

        AgentTask task = taskSelector.select(
                AgentIntent.JOB_MATCH,
                "Analyze JD requirements"
        );

        String prompt = createRuntime().buildPrompt(
                task,
                "Base prompt"
        );

        assertEquals(AgentTask.JD_ANALYSIS, task);

        assertTrue(prompt.contains(
                "Selected Agent Skill: jd-analysis"
        ));

        assertFalse(prompt.contains(
                "Selected Agent Skill: job-matching"
        ));
    }

    @Test
    void jobMatchingShouldLoadOnlyMatchingSkill() throws Exception {

        AgentTask task = taskSelector.select(
                AgentIntent.JOB_MATCH,
                "Does my resume match this job?"
        );

        String prompt = createRuntime().buildPrompt(
                task,
                "Base prompt"
        );

        assertEquals(AgentTask.JOB_MATCHING, task);

        assertTrue(prompt.contains(
                "Selected Agent Skill: job-matching"
        ));

        assertFalse(prompt.contains(
                "Selected Agent Skill: jd-analysis"
        ));
    }

    @Test
    void unsupportedTaskShouldNotInjectSkill() throws Exception {

        AgentTask task = taskSelector.select(
                AgentIntent.GENERAL,
                "Hello"
        );

        String basePrompt = "Base prompt";

        String prompt = createRuntime().buildPrompt(
                task,
                basePrompt
        );

        assertEquals(AgentTask.UNSUPPORTED, task);
        assertEquals(basePrompt, prompt);
    }

    @Test
    void jdSkillShouldBeInjectedExactlyOnce() throws Exception {

        String prompt = createRuntime().buildPrompt(
                AgentTask.JD_ANALYSIS,
                "Base prompt"
        );

        String marker = "Selected Agent Skill: jd-analysis";

        assertEquals(
                1,
                prompt.split(
                        java.util.regex.Pattern.quote(marker),
                        -1
                ).length - 1
        );
    }
}
