package com.fangyao.agent;

import java.util.List;

public class DailyJobWorkflow {

    public void run() {

        System.out.println("===== Daily Job Search Started =====");

        MatchAgent matchAgent = null;
        StrategyAgent strategyAgent = null;

        try {

            List<JobSource> jobSources =
                    List.of(
                            new RemotiveJobSource(),
                            new RemoteOkJobSource()
                    );

            JobIngestionService ingestionService =
                    new JobIngestionService(
                            jobSources
                    );

            JobRepository jobRepository =
                    new JobRepository();

            PreFilterAgent preFilterAgent =
                    new PreFilterAgent();

            JobAnalysisAgent jobAnalysisAgent =
                    new JobAnalysisAgent();

            matchAgent =
                    new MatchAgent();

            strategyAgent =
                    new StrategyAgent();

            // ==========================
            // Step 1: Fetch jobs
            // ==========================
            List<Job> newJobs =
                    ingestionService.fetchAllJobs();

            System.out.println(
                    "[JobIngestion] Total new jobs found: "
                            + newJobs.size()
            );

            // ==========================
            // Step 2: Get unscored jobs
            // ==========================
            List<Job> unscoredJobs =
                    jobRepository.getUnscoredJobs();

            System.out.println(
                    "[Agent Workflow] Unscored jobs found: "
                            + unscoredJobs.size()
            );

            // ==========================
            // Step 3: Run agent chain
            // ==========================
            for (Job job : unscoredJobs) {

                AgentContext context =
                        new AgentContext(job);

                // ==========================
                // Agent 1: PreFilterAgent
                // ==========================
                context =
                        preFilterAgent.execute(
                                context
                        );

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

                    boolean updated =
                            jobRepository.updateMatchResult(
                                    job.getId(),
                                    filteredResult
                            );

                    if (updated) {

                        System.out.println(
                                "[PreFilterAgent] Marked job ID "
                                        + job.getId()
                                        + " as filtered."
                        );

                    } else {

                        System.out.println(
                                "[PreFilterAgent] Failed to update job ID: "
                                        + job.getId()
                        );
                    }

                    continue;
                }

                // ==========================
                // Agent 2: JobAnalysisAgent
                // ==========================
                context =
                        jobAnalysisAgent.execute(
                                context
                        );

                if (context.getJobAnalysis() == null) {

                    System.err.println(
                            "[JobAnalysisAgent] No analysis result for job ID: "
                                    + job.getId()
                    );

                    continue;
                }

                JobAnalysis analysis =
                        context.getJobAnalysis();

                System.out.println(
                        "[JobAnalysisAgent] Role: "
                                + analysis.getRoleType()
                                + " | Seniority: "
                                + analysis.getSeniority()
                );

                System.out.println(
                        "[JobAnalysisAgent] Primary skills: "
                                + analysis.getPrimarySkills()
                );

                // ==========================
                // Agent 3: MatchAgent
                // ==========================
                context =
                        matchAgent.execute(
                                context
                        );

                JobMatchResult matchResult =
                        context.getMatchResult();

                if (matchResult == null) {

                    System.err.println(
                            "[MatchAgent] No result for job ID: "
                                    + job.getId()
                    );

                    continue;
                }

                boolean updated =
                        jobRepository.updateMatchResult(
                                job.getId(),
                                matchResult
                        );

                if (updated) {

                    System.out.println(
                            "[MatchAgent] Saved result for job ID: "
                                    + job.getId()
                    );

                } else {

                    System.out.println(
                            "[MatchAgent] Failed to save result for job ID: "
                                    + job.getId()
                    );
                }

                // ==========================
                // Agent 4: StrategyAgent
                // ==========================
                MatchEvaluation evaluation =
                        context.getMatchEvaluation();

                if (evaluation != null
                        && evaluation.getOverallScore() >= 60) {

                    context =
                            strategyAgent.execute(
                                    context
                            );

                    if (context.getStrategy() == null) {

                        System.err.println(
                                "[StrategyAgent] No strategy result for job ID: "
                                        + job.getId()
                        );
                    }

                } else {

                    System.out.println(
                            "[StrategyAgent] Skipped job ID: "
                                    + job.getId()
                                    + " because match score is below threshold."
                    );
                }
            }

            // ==========================
            // Step 4: Email credentials
            // ==========================
            String email =
                    System.getenv(
                            "EMAIL_ADDRESS"
                    );

            String appPassword =
                    System.getenv(
                            "EMAIL_APP_PASSWORD"
                    );

            if (email == null
                    || email.isBlank()) {

                System.err.println(
                        "[Email Error] EMAIL_ADDRESS is missing."
                );

                return;
            }

            if (appPassword == null
                    || appPassword.isBlank()) {

                System.err.println(
                        "[Email Error] EMAIL_APP_PASSWORD is missing."
                );

                return;
            }

            // ==========================
            // Step 5: Shared email service
            // ==========================
            EmailService emailService =
                    new EmailService(
                            email,
                            appPassword
                    );

            // ==========================
            // Step 6: High-match email
            // ==========================
            SentJobRepository sentJobRepository =
                    new SentJobRepository();

            JobEmailWorkflow emailWorkflow =
                    new JobEmailWorkflow(
                            sentJobRepository,
                            emailService
                    );

            emailWorkflow.run(
                    email
            );

            // ==========================
            // Step 7: Follow-up reminder
            // ==========================
            FollowUpReminderService followUpReminderService =
                    new FollowUpReminderService();

            FollowUpReminderWorkflow followUpReminderWorkflow =
                    new FollowUpReminderWorkflow(
                            followUpReminderService,
                            emailService
                    );

            followUpReminderWorkflow.run(
                    email
            );

        } catch (Exception e) {

            System.err.println(
                    "[Daily Job Workflow Error]"
            );

            e.printStackTrace();

        } finally {

            if (strategyAgent != null) {

                strategyAgent.close();
            }

            if (matchAgent != null) {

                matchAgent.close();
            }

            System.out.println(
                    "===== Daily Job Search Finished ====="
            );
        }
    }
}