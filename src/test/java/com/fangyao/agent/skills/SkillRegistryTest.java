
package com.fangyao.agent.skills;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fangyao.agent.AgentIntent;

public class SkillRegistryTest {

    @Test
    void shouldLoadBothSkills() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        assertEquals(2, registry.size());
    }

    @Test
    void shouldSelectResumeSkill() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        Optional<SkillDefinition> result =
                registry.findByIntent(AgentIntent.RESUME);

        assertTrue(result.isPresent());

        SkillDefinition skill = result.get();

        assertEquals("resume-analysis", skill.name());
        assertFalse(skill.description().isBlank());
        assertTrue(
                skill.instructions().contains(
                        "Retrieve relevant resume chunks"
                )
        );
    }

    @Test
    void shouldSelectJobMatchingSkill() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        Optional<SkillDefinition> result =
                registry.findByIntent(AgentIntent.JOB_MATCH);

        assertTrue(result.isPresent());

        SkillDefinition skill = result.get();

        assertEquals("job-matching", skill.name());
        assertTrue(
                skill.instructions().contains(
                        "Calculate a weighted matching score"
                )
        );
    }

    @Test
    void unsupportedIntentShouldReturnEmpty() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        assertTrue(
                registry.findByIntent(
                        AgentIntent.GENERAL
                ).isEmpty()
        );

        assertTrue(
                registry.findByIntent(
                        AgentIntent.JOB_SEARCH
                ).isEmpty()
        );
    }
}
