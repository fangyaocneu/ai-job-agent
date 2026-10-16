
package com.fangyao.agent.skills;

import java.io.IOException;
import java.util.Optional;

import com.fangyao.agent.AgentIntent;

public class SkillRuntime {

    private final SkillSelector selector;
    private final SkillPromptComposer composer;

    public SkillRuntime(SkillSelector selector) {
        this.selector = selector;
        this.composer = new SkillPromptComposer();
    }

    // V3 compatibility
    public String buildPrompt(
            AgentIntent intent,
            String basePrompt
    ) throws IOException {

        return appendSkill(
                selector.selectAndLoad(intent),
                basePrompt
        );
    }

    // V4: task-aware skill loading
    public String buildPrompt(
            AgentTask task,
            String basePrompt
    ) throws IOException {

        return appendSkill(
                selector.selectAndLoadByTask(task),
                basePrompt
        );
    }

    private String appendSkill(
            Optional<SkillDefinition> selected,
            String basePrompt
    ) {

        if (selected.isEmpty()) {
            return basePrompt;
        }

        SkillDefinition skill = selected.get();

        return composer.compose(
                basePrompt,
                """
                Selected Agent Skill: %s

                Skill Instructions:
                %s
                """.formatted(
                        skill.name(),
                        skill.instructions()
                )
        );
    }
}
