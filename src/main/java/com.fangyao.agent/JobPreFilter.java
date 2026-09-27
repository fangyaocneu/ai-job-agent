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

    // Senior is intentionally allowed.
    // Only Staff / Principal / Lead / Manager / Director+ are rejected.
    private static final List<String> SENIORITY_EXCLUDES = List.of(
            "senior staff",
            "staff engineer",
            "staff software",
            "staff backend",
            "staff fullstack",
            "staff full stack",
            "principal",
            "lead engineer",
            "tech lead",
            "technical lead",
            "engineering manager",
            "manager, engineering",
            "software architect",
            "solutions architect",
            "director",
            "vice president",
            "vp "
    );

    private static final List<String> NORTH_AMERICA_KEYWORDS = List.of(
            "united states",
            "usa",
            "u.s.",
            "us ",
            "u.s.a",
            "canada",
            "mexico",
            "north america"
    );

    private static final List<String> US_STATE_KEYWORDS = List.of(
            "california",
            "new york",
            "texas",
            "washington",
            "massachusetts",
            "illinois",
            "florida",
            "georgia",
            "virginia",
            "maryland",
            "colorado",
            "oregon",
            "arizona",
            "north carolina",
            "south carolina",
            "pennsylvania",
            "new jersey",
            "connecticut",
            "ohio",
            "michigan",
            "minnesota",
            "tennessee",
            "utah"
    );

    private static final List<String> NON_NORTH_AMERICA_KEYWORDS = List.of(
            "brazil",
            "india",
            "singapore",
            "japan",
            "china",
            "taiwan",
            "hong kong",
            "korea",
            "south korea",
            "australia",
            "new zealand",
            "united kingdom",
            "uk",
            "london",
            "ireland",
            "germany",
            "france",
            "spain",
            "italy",
            "netherlands",
            "sweden",
            "poland",
            "romania",
            "portugal",
            "europe",
            "emea",
            "apac",
            "asia"
    );

    public boolean shouldScore(Job job) {

        String title =
                normalize(job.getTitle());

        String location =
                normalize(job.getLocation());

        // ==========================
        // 1. Remove irrelevant roles
        // ==========================
        for (String excluded : EXCLUDE_KEYWORDS) {

            if (title.contains(excluded)) {
                return false;
            }
        }

        // ==========================
        // 2. Remove Staff+ roles
        // ==========================
        for (String senior : SENIORITY_EXCLUDES) {

            if (title.contains(senior)) {
                return false;
            }
        }

        // ==========================
        // 3. Check relevant SWE role
        // ==========================
        boolean relevantRole =
                false;

        for (String relevant : RELEVANT_KEYWORDS) {

            if (title.contains(relevant)) {

                relevantRole =
                        true;

                break;
            }
        }

        if (!relevantRole) {
            return false;
        }

        // ==========================
        // 4. North America location
        // ==========================
        if (!isNorthAmericaLocation(location)) {
            return false;
        }

        return true;
    }

    private boolean isNorthAmericaLocation(
            String location
    ) {

        // Some job sources may not provide location.
        // Do not reject missing location yet.
        if (location == null
                || location.isBlank()) {

            return true;
        }

        // Explicitly reject known non-North-America locations.
        for (String excluded : NON_NORTH_AMERICA_KEYWORDS) {

            if (location.contains(excluded)) {
                return false;
            }
        }

        // Generic remote roles are allowed because
        // some providers omit the exact region.
        if (location.equals("remote")
                || location.contains("remote - us")
                || location.contains("remote us")
                || location.contains("remote, us")
                || location.contains("remote - canada")
                || location.contains("remote canada")
                || location.contains("remote - north america")
                || location.contains("remote north america")) {

            return true;
        }

        // Explicit North America location.
        for (String allowed : NORTH_AMERICA_KEYWORDS) {

            if (location.contains(allowed)) {
                return true;
            }
        }

        // Common US states.
        for (String state : US_STATE_KEYWORDS) {

            if (location.contains(state)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .trim();
    }
}