package com.fangyao.agent;

import java.util.List;

public class DailyJobWorkflow {

    public void run() {

        System.out.println("===== Daily Job Search Started =====");

        AgentCoordinator coordinator = null;

        try {

            // ==========================
            // Step 1: Job ingestion
            // ==========================
            List<JobSource> jobSources = List.of(
                    new RemotiveJobSource(),
                    new RemoteOkJobSource(),
                    new GreenhouseJobSource(),
                    new LeverJobSource()
            );

            JobIngestionService ingestionService =
                    new JobIngestionService(jobSources);

            JobRepository jobRepository =
                    new JobRepository();

            JobAgentResultRepository agentResultRepository =
                    new JobAgentResultRepository();

            List<Job> newJobs =
                    ingestionService.fetchAllJobs();

            System.out.println(
                    "[JobIngestion] Total new jobs found: "
                            + newJobs.size()
            );

            // ==========================
            // Step 2: Get jobs to process
            // ==========================
            List<Job> unscoredJobs =
                    jobRepository.getUnscoredJobs();

            System.out.println(
                    "[Agent Workflow] Unscored jobs found: "
                            + unscoredJobs.size()
            );

            // ==========================
            // Step 3: Agent pipeline
            // ==========================
            coordinator =
                    new AgentCoordinator();

            for (Job job : unscoredJobs) {

                AgentContext context =
                        coordinator.process(job);

                JobMatchResult matchResult =
                        context.getMatchResult();

                if (matchResult == null) {

                    System.err.println(
                            "[DailyJobWorkflow] No match result for job ID: "
                                    + job.getId()
                    );

                    continue;
                }

                // ==========================
                // Save legacy match result
                // ==========================
                boolean updated =
                        jobRepository.updateMatchResult(
                                job.getId(),
                                matchResult
                        );

                if (updated) {

                    System.out.println(
                            "[DailyJobWorkflow] Saved match result for job ID: "
                                    + job.getId()
                    );

                } else {

                    System.err.println(
                            "[DailyJobWorkflow] Failed to save match result for job ID: "
                                    + job.getId()
                    );
                }

                // ==========================
                // Save structured agent result
                // ==========================
                agentResultRepository.saveOrUpdate(
                        job.getId(),
                        context.getJobAnalysis(),
                        context.getMatchEvaluation(),
                        context.getStrategy()
                );

                // ==========================
                // Strategy logging
                // ==========================
                ApplicationStrategy strategy =
                        context.getStrategy();

                if (strategy != null) {

                    System.out.println(
                            "[DailyJobWorkflow] Strategy available for job ID: "
                                    + job.getId()
                    );

                } else {

                    System.out.println(
                            "[DailyJobWorkflow] No strategy generated for job ID: "
                                    + job.getId()
                    );
                }
            }

            // ==========================
            // Step 4: Email credentials
            // ==========================
            String email =
                    System.getenv("EMAIL_ADDRESS");

            String appPassword =
                    System.getenv("EMAIL_APP_PASSWORD");

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
            // Step 5: Email service
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

            emailWorkflow.run(email);

            // ==========================
            // Step 7: Follow-up reminders
            // ==========================
            FollowUpReminderService followUpReminderService =
                    new FollowUpReminderService();

            FollowUpReminderWorkflow followUpReminderWorkflow =
                    new FollowUpReminderWorkflow(
                            followUpReminderService,
                            emailService
                    );

            followUpReminderWorkflow.run(email);

        } catch (Exception e) {

            System.err.println(
                    "[Daily Job Workflow Error]"
            );

            e.printStackTrace();

        } finally {

            if (coordinator != null) {
                coordinator.close();
            }

            System.out.println(
                    "===== Daily Job Search Finished ====="
            );
        }
    }
}