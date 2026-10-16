
package com.fangyao.agent.skills;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.fangyao.agent.AgentIntent;

public class TaskSelectorTest {

    private final TaskSelector taskSelector =
            new TaskSelector();

    @Test
    void shouldSelectJdAnalysis() {
        assertEquals(
                AgentTask.JD_ANALYSIS,
                taskSelector.select(
                        AgentIntent.JOB_MATCH,
                        "Analyze JD requirements"
                )
        );
    }

    @Test
    void shouldSelectJobMatching() {
        assertEquals(
                AgentTask.JOB_MATCHING,
                taskSelector.select(
                        AgentIntent.JOB_MATCH,
                        "Does my resume match this job?"
                )
        );
    }

    @Test
    void shouldPreserveResumeIntent() {
        assertEquals(
                AgentTask.RESUME_ANALYSIS,
                taskSelector.select(
                        AgentIntent.RESUME,
                        "Analyze my resume"
                )
        );
    }

    @Test
    void shouldHandleUnsupportedIntent() {
        assertEquals(
                AgentTask.UNSUPPORTED,
                taskSelector.select(
                        AgentIntent.GENERAL,
                        "Hello"
                )
        );
    }

    @Test
    void shouldLoadJdSkill() throws Exception {
        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        SkillSelector selector =
                new SkillSelector(registry);

        SkillDefinition skill = selector
                .selectAndLoadByTask(AgentTask.JD_ANALYSIS)
                .orElseThrow();

        assertEquals("jd-analysis", skill.name());
        assertFalse(skill.instructions().isBlank());
    }
}
