package com.fangyao.agent;

public class AgentCoordinator implements AutoCloseable {

    private static final int STRATEGY_THRESHOLD = 60;

    private static final int MAX_ATTEMPTS = 2;

    private final PreFilterAgent preFilterAgent;
    private final JobAnalysisAgent jobAnalysisAgent;
    private final MatchAgent matchAgent;
    private final StrategyAgent strategyAgent;

    private final AgentRunRepository agentRunRepository;

    public AgentCoordinator() {

        this.preFilterAgent =
                new PreFilterAgent();

        this.jobAnalysisAgent =
                new JobAnalysisAgent();

        this.matchAgent =
                new MatchAgent();

        this.strategyAgent =
                new StrategyAgent();

        this.agentRunRepository =
                new AgentRunRepository();
    }

    public AgentContext process(Job job) {

        System.out.println();
        System.out.println(
                "===== AGENT PIPELINE START ====="
        );

        System.out.println(
                "[AgentCoordinator] Processing job ID: "
                        + job.getId()
                        + " | "
                        + job.getTitle()
        );

        AgentContext context =
                new AgentContext(job);

        // ==========================
        // Agent 1: PreFilterAgent
        // ==========================
        context =
                executeAgentWithRetry(
                        "PreFilterAgent",
                        job,
                        context,
                        preFilterAgent
                );

        if (context == null) {

            System.err.println(
                    "[AgentCoordinator] PreFilterAgent failed after retries."
            );

            printPipelineEnd(job);

            return new AgentContext(job);
        }

        if (!context.isRelevant()) {

            JobMatchResult filteredResult =
                    new JobMatchResult(
                            0,
                            "Rejected by pre-filter.",
                            "Job title or seniority does not match target software engineering roles."
                    );

            context.setMatchResult(
                    filteredResult
            );

            System.out.println(
                    "[AgentCoordinator] Pipeline stopped after PreFilterAgent."
            );

            printPipelineEnd(job);

            return context;
        }

        // ==========================
        // Agent 2: JobAnalysisAgent
        // ==========================
        context =
                executeAgentWithRetry(
                        "JobAnalysisAgent",
                        job,
                        context,
                        jobAnalysisAgent
                );

        if (context == null
                || context.getJobAnalysis() == null) {

            System.err.println(
                    "[AgentCoordinator] JobAnalysisAgent failed after retries."
            );

            printPipelineEnd(job);

            return context != null
                    ? context
                    : new AgentContext(job);
        }

        // ==========================
        // Agent 3: MatchAgent
        // ==========================
        context =
                executeAgentWithRetry(
                        "MatchAgent",
                        job,
                        context,
                        matchAgent
                );

        if (context == null
                || context.getMatchEvaluation() == null
                || context.getMatchResult() == null) {

            System.err.println(
                    "[AgentCoordinator] MatchAgent failed after retries."
            );

            printPipelineEnd(job);

            return context != null
                    ? context
                    : new AgentContext(job);
        }

        // ==========================
        // Agent 4: StrategyAgent
        // ==========================
        MatchEvaluation evaluation =
                context.getMatchEvaluation();

        if (evaluation.getOverallScore()
                >= STRATEGY_THRESHOLD) {

            context =
                    executeAgentWithRetry(
                            "StrategyAgent",
                            job,
                            context,
                            strategyAgent
                    );

            if (context == null) {

                System.err.println(
                        "[AgentCoordinator] StrategyAgent failed after retries."
                );

                printPipelineEnd(job);

                return new AgentContext(job);
            }

        } else {

            System.out.println(
                    "[AgentCoordinator] StrategyAgent skipped. "
                            + "Score "
                            + evaluation.getOverallScore()
                            + " is below threshold "
                            + STRATEGY_THRESHOLD
                            + "."
            );
        }

        printPipelineEnd(job);

        return context;
    }

    private AgentContext executeAgentWithRetry(
            String agentName,
            Job job,
            AgentContext context,
            Agent<AgentContext, AgentContext> agent
    ) {

        AgentContext currentContext =
                context;

        for (
                int attempt = 1;
                attempt <= MAX_ATTEMPTS;
                attempt++
        ) {

            long runId =
                    agentRunRepository.startRun(
                            job.getId(),
                            agentName
                    );

            long startTime =
                    System.currentTimeMillis();

            try {

                System.out.println(
                        "[AgentCoordinator] "
                                + agentName
                                + " attempt "
                                + attempt
                                + "/"
                                + MAX_ATTEMPTS
                );

                // ==========================
                // Controlled retry test
                // ==========================
                String simulatedFailure =
                        System.getenv(
                                "SIMULATE_AGENT_FAILURE"
                        );

                if (attempt == 1
                        && agentName.equals(
                                simulatedFailure
                        )) {

                    throw new RuntimeException(
                            "Simulated first-attempt failure for retry test."
                    );
                }

                AgentContext result =
                        agent.execute(
                                currentContext
                        );

                validateAgentResult(
                        agentName,
                        result
                );

                long durationMs =
                        System.currentTimeMillis()
                                - startTime;

                if (runId != -1) {

                    agentRunRepository.markSuccess(
                            runId,
                            durationMs
                    );
                }

                if (attempt > 1) {

                    System.out.println(
                            "[AgentCoordinator] "
                                    + agentName
                                    + " recovered on retry attempt "
                                    + attempt
                    );
                }

                return result;

            } catch (Exception e) {

                long durationMs =
                        System.currentTimeMillis()
                                - startTime;

                String errorMessage =
                        buildErrorMessage(
                                e
                        );

                if (runId != -1) {

                    agentRunRepository.markFailed(
                            runId,
                            durationMs,
                            errorMessage
                    );
                }

                System.err.println(
                        "[AgentCoordinator] "
                                + agentName
                                + " attempt "
                                + attempt
                                + " failed: "
                                + errorMessage
                );

                if (attempt < MAX_ATTEMPTS) {

                    System.out.println(
                            "[AgentCoordinator] Retrying "
                                    + agentName
                                    + "..."
                    );

                    sleepBeforeRetry();

                } else {

                    System.err.println(
                            "[AgentCoordinator] "
                                    + agentName
                                    + " exhausted all retry attempts."
                    );
                }
            }
        }

        return null;
    }

    private void validateAgentResult(
            String agentName,
            AgentContext context
    ) {

        if (context == null) {

            throw new IllegalStateException(
                    agentName
                            + " returned null AgentContext."
            );
        }

        switch (agentName) {

            case "JobAnalysisAgent" -> {

                if (context.getJobAnalysis()
                        == null) {

                    throw new IllegalStateException(
                            "JobAnalysisAgent returned no JobAnalysis."
                    );
                }
            }

            case "MatchAgent" -> {

                if (context.getMatchEvaluation()
                        == null
                        || context.getMatchResult()
                        == null) {

                    throw new IllegalStateException(
                            "MatchAgent returned incomplete match result."
                    );
                }
            }

            case "StrategyAgent" -> {

                if (context.getStrategy()
                        == null) {

                    throw new IllegalStateException(
                            "StrategyAgent returned no ApplicationStrategy."
                    );
                }
            }

            default -> {
                // PreFilterAgent only needs a non-null AgentContext.
            }
        }
    }

    private String buildErrorMessage(
            Exception e
    ) {

        String message =
                e.getMessage();

        if (message == null
                || message.isBlank()) {

            return e.getClass()
                    .getSimpleName();
        }

        return e.getClass()
                .getSimpleName()
                + ": "
                + message;
    }

    private void sleepBeforeRetry() {

        try {

            Thread.sleep(
                    1500
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new RuntimeException(
                    "Retry sleep interrupted.",
                    e
            );
        }
    }

    private void printPipelineEnd(
            Job job
    ) {

        System.out.println(
                "[AgentCoordinator] Finished job ID: "
                        + job.getId()
        );

        System.out.println(
                "===== AGENT PIPELINE END ====="
        );

        System.out.println();
    }

    @Override
    public void close() {

        strategyAgent.close();
        matchAgent.close();
        jobAnalysisAgent.close();
    }
}