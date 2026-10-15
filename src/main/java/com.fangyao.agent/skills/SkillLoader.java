
package com.fangyao.agent.skills;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SkillLoader {

    public SkillMetadata loadMetadata(Path skillFile)
            throws IOException {

        try (BufferedReader reader = Files.newBufferedReader(
                skillFile, StandardCharsets.UTF_8)) {

            String firstLine = reader.readLine();

            if (firstLine != null) {
                firstLine = firstLine.replace("\uFEFF", "");
            }

            if (!"---".equals(firstLine)) {
                throw new IllegalArgumentException(
                        "Missing YAML frontmatter: " + skillFile
                );
            }

            Map<String, String> fields = new HashMap<>();
            String line;
            boolean closed = false;

            while ((line = reader.readLine()) != null) {

                if ("---".equals(line.trim())) {
                    closed = true;
                    break;
                }

                int colon = line.indexOf(':');

                if (colon <= 0) {
                    continue;
                }

                String key = line.substring(0, colon).trim();
                String value = line.substring(colon + 1).trim();

                fields.put(key, value);
            }

            if (!closed) {
                throw new IllegalArgumentException(
                        "Unclosed YAML frontmatter: " + skillFile
                );
            }

            String name = fields.get("name");
            String description = fields.get("description");

            if (name == null || name.isBlank()
                    || description == null
                    || description.isBlank()) {

                throw new IllegalArgumentException(
                        "Incomplete skill metadata: " + skillFile
                );
            }

            return new SkillMetadata(
                    name,
                    description,
                    skillFile
            );
        }
    }

    public SkillDefinition load(Path skillFile)
            throws IOException {

        SkillMetadata metadata = loadMetadata(skillFile);

        String content = Files.readString(
                skillFile,
                StandardCharsets.UTF_8
        );

        content = content
                .replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        int end = content.indexOf("\n---\n", 4);

        if (end < 0) {
            throw new IllegalArgumentException(
                    "Invalid YAML frontmatter: " + skillFile
            );
        }

        String instructions =
                content.substring(end + 5).trim();

        if (instructions.isBlank()) {
            throw new IllegalArgumentException(
                    "Empty skill instructions: " + skillFile
            );
        }

        return new SkillDefinition(
                metadata.name(),
                metadata.description(),
                instructions
        );
    }
}
