
package com.fangyao.agent.skills;

import java.nio.file.Path;

public record SkillMetadata(
        String name,
        String description,
        Path filePath
) {
}
