package com.fangyao.agent;

import com.openai.client.OpenAIClient;

public class MatchAgent
        implements Agent<AgentContext, AgentContext> {

    private final AIJobMatcher matcher;

    public MatchAgent(
            OpenAIClient client
    ) {

        this.matcher =
                new AIJobMatcher(
                        client
                );
    }

    @Override
    public AgentContext execute(
            AgentContext context
    ) {

        Job job =
                context.getJob();

        JobAnalysis analysis =
                context.getJobAnalysis();

        System.out.println(
                "[MatchAgent] Scoring job ID: "
                        + job.getId()
                        + " | "
                        + job.getTitle()
        );

        try {

            MatchEvaluation evaluation =
                    matcher.evaluateJob(
                            job,
                            analysis
                    );

            JobMatchResult matchResult =
                    evaluation.toJobMatchResult();

            context.setMatchEvaluation(
                    evaluation
            );

            context.setMatchResult(
                    matchResult
            );

            System.out.println(
                    "[MatchAgent] Completed job ID: "
                            + job.getId()
            );

            return context;

        } catch (Exception e) {

            throw new RuntimeException(
                    "MatchAgent failed for job ID: "
                            + job.getId(),
                    e
            );
        }
    }
}