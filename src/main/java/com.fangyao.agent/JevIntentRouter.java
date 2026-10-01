package com.fangyao.agent;

public class JevIntentRouter {

    private static final double MIN_CONFIDENCE = 0.70;

    private final JevClient jevClient;

    public JevIntentRouter() {
        this.jevClient =
                new JevClient();
    }

    public AgentIntent route(
            String message
    ) {

        JevClient.JevDecision decision =
                jevClient.classifyIntent(
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

            return AgentIntent.GENERAL;
        }

        try {

            return AgentIntent.valueOf(
                    decision.getChoice()
                            .toUpperCase()
                            .trim()
            );

        } catch (IllegalArgumentException e) {

            return AgentIntent.GENERAL;
        }
    }
}