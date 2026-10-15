
package com.fangyao.agent.skills;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SkillLoader {

    public SkillDefinition load(Path skillFile) throws IOException {

        String content = Files.readString(
                skillFile,
                StandardCharsets.UTF_8
        );

        String normalized = content.replace("\r\n", "\n");

        if (!normalized.startsWith("---\n")) {
            throw new IllegalArgumentException(
                    "Missing YAML frontmatter: " + skillFile
            );
        }

        int end = normalized.indexOf("\n---\n", 4);

        if (end < 0) {
            throw new IllegalArgumentException(
                    "Invalid YAML frontmatter: " + skillFile
            );
        }

        String metadata = normalized.substring(4, end);
        String instructions = normalized.substring(end + 5).trim();

        // Minimal parser for simple name/description metadata.
        // A YAML parser will be added if more fields are needed.
        Map<String, String> fields = new HashMap<>();

        for (String line : metadata.split("\n")) {

            int colon = line.indexOf(':');

            if (colon < 0) {
                continue;
            }

            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();

            fields.put(key, value);
        }

        String name = fields.get("name");
        String description = fields.get("description");

        if (name == null || name.isBlank()
                || description == null || description.isBlank()
                || instructions.isBlank()) {

            throw new IllegalArgumentException(
                    "Incomplete skill definition: " + skillFile
            );
        }

        return new SkillDefinition(
                name,
                description,
                instructions
        );
    }
}
