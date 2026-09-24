package com.fangyao.agent;

import java.util.List;

public class DailyJobRunner {

    public static void main(String[] args) {

        System.out.println(
                "===== Daily Job Search Started ====="
        );

        SearchJobs searchJobs =
                new SearchJobs();

        AIJobMatcher matcher =
                new AIJobMatcher();

        JobRepository jobRepository =
                new JobRepository();

        
        // =========================
        // REAL JOB SEARCH
        // =========================
        String[] keywords = {
                "Java",
                "backend",
                "software engineer"
        };

        for (String keyword : keywords) {

            System.out.println(
                    "[SearchJobs] Searching: "
                            + keyword
            );

            List<Job> newJobs =
                    searchJobs.search(keyword);

            if (newJobs.isEmpty()) {

                System.out.println(
                        "No new jobs found for keyword: "
                                + keyword
                );

                continue;
            }

            for (Job job : newJobs) {

                try {

                    JobMatchResult result =
                            matcher.scoreJob(job);

                    boolean updated =
                            jobRepository.updateMatchResult(
                                    job.getId(),
                                    result
                            );

                    if (updated) {

                        System.out.println(
                                "[AI Matcher] Match result saved for: "
                                        + job.getTitle()
                        );

                    } else {

                        System.out.println(
                                "[AI Matcher] Failed to save match result for: "
                                        + job.getTitle()
                        );
                    }

                } catch (Exception e) {

                    System.out.println(
                            "[AI Matcher Error] "
                                    + job.getTitle()
                                    + " @ "
                                    + job.getCompany()
                                    + ": "
                                    + e.getMessage()
                    );
                }
            }
        }

        // =========================
        // EMAIL
        // =========================
        String email =
                System.getenv("EMAIL_ADDRESS");

        String appPassword =
                System.getenv("EMAIL_APP_PASSWORD");

        if (email == null || email.isBlank()) {

            System.out.println(
                    "[Email Error] EMAIL_ADDRESS is missing."
            );

            return;
        }

        if (appPassword == null || appPassword.isBlank()) {

            System.out.println(
                    "[Email Error] EMAIL_APP_PASSWORD is missing."
            );

            return;
        }

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

        System.out.println(
                "===== Daily Job Search Finished ====="
        );
    }
}