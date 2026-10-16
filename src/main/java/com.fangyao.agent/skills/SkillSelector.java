
package com.fangyao.agent.skills;

import java.io.IOException;
import java.util.Optional;

import com.fangyao.agent.AgentIntent;

public class SkillSelector {

    private final SkillRegistry registry;
    private final SkillLoader loader;

    public SkillSelector(SkillRegistry registry) {
        this.registry = registry;
        this.loader = new SkillLoader();
    }

    // V2: Existing intent-based selection
    public Optional<SkillMetadata> select(
            AgentIntent intent
    ) {
        return registry.findByIntent(intent);
    }

    public Optional<SkillDefinition> selectAndLoad(
            AgentIntent intent
    ) throws IOException {

        Optional<SkillMetadata> selected = select(intent);

        if (selected.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                loader.load(selected.get().filePath())
        );
    }

    // V4: Task-based selection
    public Optional<SkillMetadata> selectByTask(
            AgentTask task
    ) {
        if (task == null) {
            return Optional.empty();
        }

        String skillName = switch (task) {
            case RESUME_ANALYSIS -> "resume-analysis";
            case JD_ANALYSIS -> "jd-analysis";
            case JOB_MATCHING -> "job-matching";
            case UNSUPPORTED -> null;
        };

        if (skillName == null) {
            return Optional.empty();
        }

        return registry.findByName(skillName);
    }

    public Optional<SkillDefinition> selectAndLoadByTask(
            AgentTask task
    ) throws IOException {

        Optional<SkillMetadata> metadata =
                selectByTask(task);

        if (metadata.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                loader.load(metadata.get().filePath())
        );
    }
}
