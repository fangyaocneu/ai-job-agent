
package com.fangyao.agent.skills;

import com.fangyao.agent.AgentIntent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class SkillRegistry {

    private final Map<AgentIntent, SkillMetadata> skills =
            new EnumMap<>(AgentIntent.class);

    private final Map<String, SkillMetadata> skillsByName =
            new HashMap<>();

    private final SkillLoader loader = new SkillLoader();

    public SkillRegistry(Path skillsDirectory)
            throws IOException {

        discover(skillsDirectory);

        register(AgentIntent.RESUME, "resume-analysis");
        register(AgentIntent.JOB_MATCH, "job-matching");
    }

    private void discover(Path directory)
            throws IOException {

        if (!Files.isDirectory(directory)) {
            throw new IOException(
                    "Skills directory not found: " + directory
            );
        }

        try (Stream<Path> paths = Files.walk(directory)) {

            for (Path path : paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName()
                            .toString().equals("SKILL.md"))
                    .sorted()
                    .toList()) {

                SkillMetadata metadata =
                        loader.loadMetadata(path);

                if (skillsByName.putIfAbsent(
                        metadata.name(), metadata) != null) {

                    throw new IllegalArgumentException(
                            "Duplicate skill name: "
                                    + metadata.name()
                    );
                }
            }
        }
    }

    private void register(
            AgentIntent intent,
            String skillName
    ) {

        SkillMetadata metadata = skillsByName.get(skillName);

        if (metadata == null) {
            throw new IllegalArgumentException(
                    "Skill not found: " + skillName
            );
        }

        skills.put(intent, metadata);
    }

    public Optional<SkillMetadata> findByIntent(
            AgentIntent intent
    ) {
        return Optional.ofNullable(skills.get(intent));
    }

    public Optional<SkillMetadata> findByName(
            String name
    ) {
        return Optional.ofNullable(skillsByName.get(name));
    }

    public Map<String, SkillMetadata> listSkills() {
        return Collections.unmodifiableMap(skillsByName);
    }

    public int size() {
        return skills.size();
    }
}
