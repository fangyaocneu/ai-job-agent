
package com.fangyao.agent.skills;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.fangyao.agent.AgentIntent;

public class SkillRegistry {

    private final Map<AgentIntent, SkillDefinition> skills =
            new EnumMap<>(AgentIntent.class);

    private final SkillLoader loader = new SkillLoader();

    public SkillRegistry(Path skillsDirectory) throws IOException {

        register(
                AgentIntent.RESUME,
                skillsDirectory.resolve("resume-analysis/SKILL.md")
        );

        register(
                AgentIntent.JOB_MATCH,
                skillsDirectory.resolve("job-matching/SKILL.md")
        );
    }

    private void register(
            AgentIntent intent,
            Path path
    ) throws IOException {

        SkillDefinition skill = loader.load(path);

        skills.put(intent, skill);
    }

    public Optional<SkillDefinition> findByIntent(
            AgentIntent intent
    ) {

        return Optional.ofNullable(skills.get(intent));
    }

    public int size() {
        return skills.size();
    }
}
