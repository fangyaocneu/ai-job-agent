package com.fangyao.agent;

import java.util.List;

public class DailyJobRunner {

    public static void main(String[] args) {

        System.out.println("===== Daily Job Search Started =====");

        AIJobMatcher matcher = null;

        try {
            SearchJobs searchJobs = new SearchJobs();
            JobRepository jobRepository = new JobRepository();
            JobPreFilter preFilter = new JobPreFilter();

            matcher = new AIJobMatcher();

            String[] keywords = {
                    "Java",
                    "backend",
                    "software engineer"
            };

            // Step 1: Search new jobs and save them into database
            for (String keyword : keywords) {

                List<Job> newJobs = searchJobs.search(keyword);

                if (newJobs.isEmpty()) {
                    System.out.println(
                            "No new jobs found for keyword: " + keyword
                    );
                } else {
                    System.out.println(
                            "Found " + newJobs.size()
                                    + " new jobs for keyword: "
                                    + keyword
                    );
                }
            }

            // Step 2: Find jobs that have not been scored yet
            List<Job> unscoredJobs =
                    jobRepository.getUnscoredJobs();

            System.out.println(
                    "[AI Matcher] Unscored jobs found: "
                            + unscoredJobs.size()
            );

            // Step 3: Pre-filter and score relevant jobs
            for (Job job : unscoredJobs) {

                // Skip obviously irrelevant jobs before calling OpenAI
                if (!preFilter.shouldScore(job)) {

                        System.out.println(
                        "[PreFilter] Skipping irrelevant job ID: "
                        + job.getId()
                        + " | "
                        + job.getTitle()
                );

                JobMatchResult filteredResult =
                        new JobMatchResult(
                    0,
                    "Rejected by pre-filter.",
                    "Job title or seniority does not match target software engineering roles."
                );

                jobRepository.updateMatchResult(
                 job.getId(),
                filteredResult
                );

                continue;
                }

                try {
                    System.out.println(
                            "[AI Matcher] Scoring job ID: "
                                    + job.getId()
                                    + " | "
                                    + job.getTitle()
                    );

                    JobMatchResult result =
                            matcher.scoreJob(job);

                    boolean updated =
                            jobRepository.updateMatchResult(
                                    job.getId(),
                                    result
                            );

                    if (updated) {
                        System.out.println(
                                "[AI Matcher] Updated job ID: "
                                        + job.getId()
                        );
                    } else {
                        System.out.println(
                                "[AI Matcher] Failed to update job ID: "
                                        + job.getId()
                        );
                    }

                } catch (Exception e) {

                    System.err.println(
                            "[AI Matcher] Failed to score job ID: "
                                    + job.getId()
                    );

                    e.printStackTrace();
                }
            }

            // Step 4: Read email credentials
            String email =
                    System.getenv("EMAIL_ADDRESS");

            String appPassword =
                    System.getenv("EMAIL_APP_PASSWORD");

            if (email == null || email.isBlank()) {
                System.err.println(
                        "[Email Error] EMAIL_ADDRESS is missing."
                );
                return;
            }

            if (appPassword == null || appPassword.isBlank()) {
                System.err.println(
                        "[Email Error] EMAIL_APP_PASSWORD is missing."
                );
                return;
            }

            // Step 5: Send high-match jobs by email
            SentJobRepository sentJobRepository =
                    new SentJobRepository();

            EmailService emailService =
                    new EmailService(
                            email,
                            appPassword
                    );

            JobEmailWorkflow emailWorkflow =
                    new JobEmailWorkflow(
                            sentJobRepository,
                            emailService
                    );

            emailWorkflow.run(email);

        } catch (Exception e) {

            System.err.println(
                    "[Daily Job Runner Error]"
            );

            e.printStackTrace();

        } finally {

            if (matcher != null) {
                matcher.close();
            }

            System.out.println(
                    "===== Daily Job Search Finished ====="
            );
        }
    }
}