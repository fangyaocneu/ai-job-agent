package com.fangyao.agent;

public class MatchAgent
        implements Agent<AgentContext, AgentContext>, AutoCloseable {

    private final AIJobMatcher matcher;

    public MatchAgent() {
        this.matcher =
                new AIJobMatcher();
    }

    @Override
    public AgentContext execute(
            AgentContext context
    ) {

        Job job =
                context.getJob();

        try {

            System.out.println(
                    "[MatchAgent] Scoring job ID: "
                            + job.getId()
                            + " | "
                            + job.getTitle()
            );

            MatchEvaluation evaluation =
                    matcher.evaluateJob(
                            job,
                            context.getJobAnalysis()
                    );

            // Phase 5 structured result
            context.setMatchEvaluation(
                    evaluation
            );

            // Backward compatibility with existing DB workflow
            JobMatchResult matchResult =
                    evaluation.toJobMatchResult();

            context.setMatchResult(
                    matchResult
            );

            System.out.println(
                    "[MatchAgent] Completed job ID: "
                            + job.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "[MatchAgent] Failed job ID: "
                            + job.getId()
            );

            e.printStackTrace();
        }

        return context;
    }

    @Override
    public void close() {

        matcher.close();
    }
}