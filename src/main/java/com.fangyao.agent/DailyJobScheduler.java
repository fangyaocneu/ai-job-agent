package com.fangyao.agent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyJobScheduler {

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private static final LocalTime RUN_TIME =
            LocalTime.of(8, 0);

    public static void main(String[] args) {

        System.out.println(
                "Daily Job Agent started."
        );

        System.out.println(
                "Scheduled email time: 8:00 AM"
        );

        scheduleNextRun();
    }

    private static void scheduleNextRun() {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime nextRun =
                now.toLocalDate()
                        .atTime(RUN_TIME);

        // If today's 8:00 AM has already passed,
        // schedule for tomorrow at 8:00 AM.
        if (!nextRun.isAfter(now)) {

            nextRun =
                    nextRun.plusDays(1);
        }

        long delaySeconds =
                Duration.between(
                        now,
                        nextRun
                ).getSeconds();

        System.out.println(
                "Next run: " + nextRun
        );

        scheduler.schedule(
                () -> {

                    try {

                        runDailyJobWorkflow();

                    } catch (Exception e) {

                        System.out.println(
                                "[Scheduler Error] "
                                        + e.getMessage()
                        );

                        e.printStackTrace();

                    } finally {

                        // After today's job finishes,
                        // calculate the next 8:00 AM again.
                        scheduleNextRun();
                    }

                },
                delaySeconds,
                TimeUnit.SECONDS
        );
    }

    private static void runDailyJobWorkflow() {

    System.out.println(
            "\n===== Daily Job Search Started ====="
    );

    String[] keywords = {
            "Java",
            "backend",
            "software engineer"
    };

    SearchJobs searchJobs =
            new SearchJobs();

    AIJobMatcher matcher =
            new AIJobMatcher();

    for (String keyword : keywords) {

        System.out.println(
                "[SearchJobs] Searching: "
                        + keyword
        );

        var newJobs =
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

                matcher.scoreJob(job);

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

    JobEmailWorkflow workflow =
            new JobEmailWorkflow(
                    sentJobRepository,
                    emailService
            );

    workflow.run(email);

    System.out.println(
            "===== Daily Job Search Finished =====\n"
    );
}
}