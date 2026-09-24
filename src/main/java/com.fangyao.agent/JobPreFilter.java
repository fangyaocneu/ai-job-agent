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

    public boolean shouldScore(Job job) {

        String title = job.getTitle() == null
                ? ""
                : job.getTitle().toLowerCase();

        for (String excluded : EXCLUDE_KEYWORDS) {
            if (title.contains(excluded)) {
                return false;
            }
        }

        for (String relevant : RELEVANT_KEYWORDS) {
            if (title.contains(relevant)) {
                return true;
            }
        }

        return false;
    }
}   