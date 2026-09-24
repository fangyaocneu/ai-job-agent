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
            "data scientist",
            "marketing",
            "payroll",
            "paralegal",
            "account executive",
            "people operations",
            "human resources",
            "hr ",
            "recruiter",
            "business development",
            "pricing",
            "analyst"
    );

    private static final List<String> SENIORITY_EXCLUDES = List.of(
            "senior",
            "sr.",
            "staff",
            "principal",
            "lead",
            "manager",
            "director",
            "architect",
            "vp",
            "vice president"
    );

    public boolean shouldScore(Job job) {

        String title = normalize(job.getTitle());

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

        // 3. Only use the title to decide whether the role is relevant
        for (String relevant : RELEVANT_KEYWORDS) {
            if (title.contains(relevant)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text.toLowerCase().trim();
    }
}