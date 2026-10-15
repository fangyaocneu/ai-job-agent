
package com.fangyao.agent.skills;

import com.fangyao.agent.AgentIntent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SkillRegistryTest {

    // Test 1: Verify both skills are registered
    @Test
    void shouldLoadBothSkills() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        assertEquals(2, registry.size());

        assertTrue(
                registry.findByName("resume-analysis").isPresent()
        );

        assertTrue(
                registry.findByName("job-matching").isPresent()
        );
    }

    // Test 2: Select resume metadata without loading instructions
    @Test
    void shouldSelectResumeSkill() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        Optional<SkillMetadata> result =
                registry.findByIntent(AgentIntent.RESUME);

        assertTrue(result.isPresent());

        SkillMetadata metadata = result.get();

        assertEquals("resume-analysis", metadata.name());
        assertFalse(metadata.description().isBlank());
        assertTrue(
                metadata.filePath().toString().endsWith("SKILL.md")
        );
    }

    // Test 3: Select job matching metadata
    @Test
    void shouldSelectJobMatchingSkill() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        Optional<SkillMetadata> result =
                registry.findByIntent(AgentIntent.JOB_MATCH);

        assertTrue(result.isPresent());

        SkillMetadata metadata = result.get();

        assertEquals("job-matching", metadata.name());
        assertFalse(metadata.description().isBlank());
    }

    // Test 4: Unsupported intents must not select a skill
    @Test
    void unsupportedIntentShouldReturnEmpty() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        assertTrue(
                registry.findByIntent(AgentIntent.GENERAL).isEmpty()
        );

        assertTrue(
                registry.findByIntent(AgentIntent.JOB_SEARCH).isEmpty()
        );

        assertTrue(
                registry.findByIntent(AgentIntent.APPLICATION).isEmpty()
        );
    }

    // Test 5: Load full resume instructions on demand
    @Test
    void shouldLoadResumeInstructionsOnDemand() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        SkillSelector selector =
                new SkillSelector(registry);

        // Step 1: Select metadata only
        Optional<SkillMetadata> selected =
                selector.select(AgentIntent.RESUME);

        assertTrue(selected.isPresent());
        assertEquals("resume-analysis", selected.get().name());

        // Step 2: Load full instructions
        SkillDefinition skill =
                selector.selectAndLoad(AgentIntent.RESUME)
                        .orElseThrow();

        assertEquals("resume-analysis", skill.name());

        assertTrue(
                skill.instructions().contains(
                        "Retrieve relevant resume chunks"
                )
        );

        assertFalse(skill.instructions().isBlank());
    }

    // Test 6: Load job matching instructions on demand
    @Test
    void shouldLoadJobMatchingInstructionsOnDemand() throws Exception {

        SkillRegistry registry =
                new SkillRegistry(Path.of("skills"));

        SkillSelector selector =
                new SkillSelector(registry);

        SkillDefinition skill =
                selector.selectAndLoad(AgentIntent.JOB_MATCH)
                        .orElseThrow();

        assertEquals("job-matching", skill.name());

        assertTrue(
                skill.instructions().contains(
                        "Calculate a weighted matching score"
                )
        );

        assertTrue(
                skill.instructions().contains(
                        "Skill Match: 40%"
                )
        );
    }
}
