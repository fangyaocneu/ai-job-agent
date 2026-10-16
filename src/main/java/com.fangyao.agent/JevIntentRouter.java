package com.fangyao.agent;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class JevIntentRouter {

    private static final double MIN_CONFIDENCE = 0.70;

    private final JevClient jevClient;

    public JevIntentRouter() {
        this(new JevClient());
    }

// Constructor injection for testing
    public JevIntentRouter(JevClient jevClient) {
        this.jevClient = java.util.Objects.requireNonNull(
                jevClient,
                "jevClient must not be null"
        );
    }

    // =========================
    // Backward-Compatible API
    // =========================
    public AgentIntent route(String message) {
        return routeWithDetails(message).intent();
    }

    // =========================
    // Routing with Observability
    // =========================
    public RoutingDecision routeWithDetails(String message) {

        long start = System.nanoTime();

        // =========================
        // Fast Local Routing
        // =========================
        AgentIntent localIntent = tryLocalRoute(message);

        if (localIntent != null) {

            System.out.println(
                    "[LOCAL ROUTE] intent=" + localIntent
            );

            return buildDecision(
                    localIntent,
                    RoutingDecision.RoutingSource.LOCAL,
                    null,
                    start,
                    null
            );
        }

        // =========================
        // Jev Availability Check
        // =========================
        if (!jevClient.isAvailable()) {

            System.out.println(
                    "[JEV FALLBACK] Jev unavailable. Defaulting to GENERAL."
            );

            return buildDecision(
                    AgentIntent.GENERAL,
                    RoutingDecision.RoutingSource.FALLBACK,
                    null,
                    start,
                    "JEV_API_KEY_MISSING"
            );
        }

        // =========================
        // Jev Classification
        // =========================
        try {

            JevClient.JevDecision decision
                    = jevClient.classifyIntent(message);

            String choice = decision.getChoice();
            double confidence = decision.getConfidence();

            System.out.println(
                    "[JEV ROUTE] choice=" + choice
                    + ", confidence=" + confidence
            );

            // =========================
            // Confidence Validation
            // =========================
            if (!Double.isFinite(confidence)
                    || confidence < 0.0
                    || confidence > 1.0) {

                System.out.println(
                        "[JEV FALLBACK] Invalid confidence."
                );

                return buildDecision(
                        AgentIntent.GENERAL,
                        RoutingDecision.RoutingSource.FALLBACK,
                        null,
                        start,
                        "INVALID_CONFIDENCE"
                );
            }

            if (confidence < MIN_CONFIDENCE) {

                System.out.println(
                        "[JEV FALLBACK] Low confidence. Defaulting to GENERAL."
                );

                return buildDecision(
                        AgentIntent.GENERAL,
                        RoutingDecision.RoutingSource.FALLBACK,
                        confidence,
                        start,
                        "LOW_CONFIDENCE"
                );
            }

            // =========================
            // Intent Validation
            // =========================
            try {

                AgentIntent intent = AgentIntent.valueOf(
                        choice.toUpperCase(Locale.ROOT).trim()
                );

                return buildDecision(
                        intent,
                        RoutingDecision.RoutingSource.JEV,
                        confidence,
                        start,
                        null
                );

            } catch (IllegalArgumentException
                    | NullPointerException e) {

                System.out.println(
                        "[JEV FALLBACK] Unknown intent returned by Jev."
                );

                return buildDecision(
                        AgentIntent.GENERAL,
                        RoutingDecision.RoutingSource.FALLBACK,
                        confidence,
                        start,
                        "UNKNOWN_INTENT"
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "[JEV ERROR] Failed to classify intent: "
                    + e.getMessage()
            );

            return buildDecision(
                    AgentIntent.GENERAL,
                    RoutingDecision.RoutingSource.FALLBACK,
                    null,
                    start,
                    "JEV_REQUEST_FAILED"
            );
        }
    }

    // =========================
    // Build Routing Decision
    // =========================
    private RoutingDecision buildDecision(
            AgentIntent intent,
            RoutingDecision.RoutingSource source,
            Double confidence,
            long start,
            String fallbackReason
    ) {

        long latencyMs = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - start
        );

        return new RoutingDecision(
                intent,
                source,
                confidence,
                latencyMs,
                fallbackReason
        );
    }

    // =========================
    // Local Fast Path
    // =========================
    private AgentIntent tryLocalRoute(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        String normalized = message
                .toLowerCase(Locale.ROOT)
                .trim();

        boolean hasUrl
                = normalized.contains("http://")
                || normalized.contains("https://");

        // =========================
        // JOB MATCH
        // =========================
        
        // =========================
        // V4: Explicit JD Analysis
        // =========================
        // Route JD analysis to JOB_MATCH.
        // TaskSelector will distinguish JD_ANALYSIS
        // from JOB_MATCHING.

        if (containsAny(
                normalized,
                "analyze jd",
                "analyse jd",
                "analyze this jd",
                "analyze this job",
                "analyze job description",
                "job description analysis",
                "分析jd",
                "解析jd",
                "分析職缺",
                "職缺要求"
        )) {
            return AgentIntent.JOB_MATCH;
        }

        // =========================
        // V4: Explicit Resume-to-Job Matching
        // =========================
        // Must run before the general RESUME rule.

        boolean mentionsResume = containsAny(
                normalized,
                "my resume",
                "my cv",
                "我的履歷"
        );

        boolean mentionsMatch = containsAny(
                normalized,
                "match",
                "fit this job",
                "fit this role",
                "qualified for",
                "匹配",
                "適合這個職缺"
        );

        if (mentionsResume && mentionsMatch) {
            return AgentIntent.JOB_MATCH;
        }

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
        // APPLICATION
        // =========================
        // Check application intent BEFORE job search.
        // This prevents existing application queries
        // from being incorrectly routed to JOB_SEARCH.
        //
        // Example:
        // "Show me jobs I already applied to"
        // Expected: APPLICATION
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

        // =========================
        // JOB SEARCH
        // =========================
        boolean mentionsJob = containsAny(
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

        boolean hasJobSearchAction = containsAny(
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

            return AgentIntent.JOB_SEARCH;
        }

        // =========================
        // Ambiguous Request
        // =========================
        // Let Jev classify it if available.
        return null;
    }

    // =========================
    // Keyword Matching Helper
    // =========================
    private boolean containsAny(
            String text,
            String... phrases
    ) {

        for (String phrase : phrases) {

            if (text.contains(phrase)) {
                return true;
            }
        }

        return false;
    }
}
