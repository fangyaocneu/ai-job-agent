package com.fangyao.agent;

public class JevIntentRouter {

    private static final double MIN_CONFIDENCE = 0.70;

    private final JevClient jevClient;

    public JevIntentRouter() {
        this.jevClient
                = new JevClient();
    }

    public AgentIntent route(
            String message
    ) {

        // =========================
        // Fast Local Routing
        // =========================
        AgentIntent localIntent
                = tryLocalRoute(
                        message
                );

        if (localIntent != null) {

            System.out.println(
                    "[LOCAL ROUTE] intent="
                    + localIntent
            );

            return localIntent;
        }

        // =========================
        // Jev Availability Check
        // =========================
        if (!jevClient.isAvailable()) {

            System.out.println(
                    "[JEV FALLBACK] Jev unavailable. Defaulting to GENERAL."
            );

            return AgentIntent.GENERAL;
        }

        // =========================
        // Jev Fallback
        // =========================
        try {

            JevClient.JevDecision decision
                    = jevClient.classifyIntent(
                            message
                    );

            System.out.println(
                    "[JEV ROUTE] choice="
                    + decision.getChoice()
                    + ", confidence="
                    + decision.getConfidence()
            );

            if (decision.getConfidence()
                    < MIN_CONFIDENCE) {

                System.out.println(
                        "[JEV FALLBACK] Low confidence. Defaulting to GENERAL."
                );

                return AgentIntent.GENERAL;
            }

            try {

                return AgentIntent.valueOf(
                        decision.getChoice()
                                .toUpperCase()
                                .trim()
                );

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "[JEV FALLBACK] Unknown intent returned by Jev. Defaulting to GENERAL."
                );

                return AgentIntent.GENERAL;
            }

        } catch (Exception e) {

            System.out.println(
                    "[JEV ERROR] Failed to classify intent: "
                    + e.getMessage()
            );

            System.out.println(
                    "[JEV FALLBACK] Defaulting to GENERAL."
            );

            return AgentIntent.GENERAL;
        }
    }

    // =========================
    // Local Fast Path
    // =========================
    private AgentIntent tryLocalRoute(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return null;
        }

        String normalized
                = message
                        .toLowerCase()
                        .trim();

        boolean hasUrl
                = normalized.contains("http://")
                || normalized.contains("https://");

        // =========================
        // JOB MATCH
        // =========================
        if (hasUrl
                && containsAny(
                        normalized,
                        "good fit",
                        "fit for",
                        "qualified",
                        "qualification",
                        "match",
                        "should i apply",
                        "job posting",
                        "job description",
                        "this job",
                        "this role"
                )) {

            return AgentIntent.JOB_MATCH;
        }

        // =========================
        // RESUME
        // =========================
        if (!hasUrl
                && containsAny(
                        normalized,
                        "my resume",
                        "resume experience",
                        "resume skills",
                        "based on my resume",
                        "according to my resume"
                )) {

            return AgentIntent.RESUME;
        }

        // =========================
        // JOB SEARCH
        // =========================
        boolean mentionsJob
                = containsAny(
                        normalized,
                        "job",
                        "jobs",
                        "opening",
                        "openings",
                        "position",
                        "positions",
                        "role",
                        "roles"
                );

        boolean hasJobSearchAction
                = containsAny(
                        normalized,
                        "find",
                        "search",
                        "show",
                        "recommend",
                        "browse",
                        "looking for"
                );

        if (!hasUrl
                && mentionsJob
                && hasJobSearchAction) {

            System.out.println(
                    "[LOCAL ROUTE] intent=JOB_SEARCH"
            );

            return AgentIntent.JOB_SEARCH;
        }

        // =========================
        // APPLICATION
        // =========================
        if (containsAny(
                normalized,
                "my application",
                "application status",
                "follow up",
                "follow-up",
                "applied to",
                "applications"
        )) {

            return AgentIntent.APPLICATION;
        }

        /*
         * Ambiguous request:
         * let Jev classify it if available.
         */
        return null;
    }

    private boolean containsAny(
            String text,
            String... phrases
    ) {

        for (String phrase : phrases) {

            if (text.contains(
                    phrase
            )) {

                return true;
            }
        }

        return false;
    }
}
