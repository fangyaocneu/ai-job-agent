package com.fangyao.agent;

import java.util.List;

public class JobPreFilter {

    private static final List<String> RELEVANT_KEYWORDS = List.of(
            "software",
            "developer",
            "engineer",
            "backend",
            "java",
            "spring",
            "api",
            "full-stack",
            "full stack"
    );

    private static final List<String> EXCLUDE_KEYWORDS = List.of(
            "sales",
            "writer",
            "copywriter",
            "office assistant",
            "customer service",
            "content reviewer",
            "data scientist"
    );

    private static final List<String> SENIORITY_EXCLUDES = List.of(
            "senior",
            "sr.",
            "staff",
            "principal",
            "lead",
            "manager",
            "director",
            "architect"
    );

    public boolean shouldScore(Job job) {

        String title = normalize(job.getTitle());
        String description = normalize(job.getDescription());

        String combined = title + " " + description;

        // 1. Remove clearly irrelevant roles
        for (String excluded : EXCLUDE_KEYWORDS) {
            if (title.contains(excluded)) {
                return false;
            }
        }

        // 2. Remove roles that are too senior
        for (String senior : SENIORITY_EXCLUDES) {
            if (title.contains(senior)) {
                return false;
            }
        }

        // 3. Keep roles that match software engineering keywords
        for (String relevant : RELEVANT_KEYWORDS) {
            if (combined.contains(relevant)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text.toLowerCase();
    }
}