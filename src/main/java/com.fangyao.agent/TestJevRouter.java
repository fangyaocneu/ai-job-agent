package com.fangyao.agent;

public class TestJevRouter {

    public static void main(
            String[] args
    ) {

        JevIntentRouter router =
                new JevIntentRouter();

        test(
                router,
                "Based on my uploaded resume, what backend experience do I have?"
        );

        test(
                router,
                "Find me Java backend jobs."
        );

        test(
                router,
                "How well do I match this software engineer role?"
        );

        test(
                router,
                "Which jobs have I applied to?"
        );

        test(
                router,
                "How should I prepare for a software engineering interview?"
        );
    }

    private static void test(
            JevIntentRouter router,
            String message
    ) {

        AgentIntent intent =
                router.route(
                        message
                );

        System.out.println(
                message
                + " -> "
                + intent
        );
    }
}