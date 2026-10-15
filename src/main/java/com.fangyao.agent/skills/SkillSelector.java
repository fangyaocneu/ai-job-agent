
package com.fangyao.agent.skills;

import com.fangyao.agent.AgentIntent;

import java.io.IOException;
import java.util.Optional;

public class SkillSelector {

    private final SkillRegistry registry;
    private final SkillLoader loader;

    public SkillSelector(SkillRegistry registry) {
        this.registry = registry;
        this.loader = new SkillLoader();
    }

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

        SkillDefinition skill = loader.load(
                selected.get().filePath()
        );

        return Optional.of(skill);
    }
}
