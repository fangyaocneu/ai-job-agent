
package com.fangyao.agent.skills;

import java.io.IOException;

import com.fangyao.agent.AgentIntent;

public class SkillRuntime {

    private final SkillSelector selector;
    private final SkillPromptComposer composer;

    public SkillRuntime(SkillSelector selector) {
        this.selector = selector;
        this.composer = new SkillPromptComposer();
    }

    public String buildPrompt(
            AgentIntent intent,
            String basePrompt
    ) throws IOException {

        var skill = selector.selectAndLoad(intent);

        if (skill.isEmpty()) {
            return basePrompt;
        }

        return composer.compose(
                basePrompt,
                """
                Selected Agent Skill: %s

                Skill Instructions:
                %s
                """.formatted(
                        skill.get().name(),
                        skill.get().instructions()
                )
        );
    }
}
