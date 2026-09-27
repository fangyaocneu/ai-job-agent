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
                "Scheduled time: 8:00 AM"
        );

        scheduleNextRun();
    }

    private static void scheduleNextRun() {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime nextRun =
                now.toLocalDate()
                        .atTime(RUN_TIME);

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
                "Next run: "
                        + nextRun
        );

        scheduler.schedule(
                () -> {

                    try {

                        DailyJobWorkflow workflow =
                                new DailyJobWorkflow();

                        workflow.run();

                    } catch (Exception e) {

                        System.err.println(
                                "[Scheduler Error]"
                        );

                        e.printStackTrace();

                    } finally {

                        scheduleNextRun();
                    }
                },
                delaySeconds,
                TimeUnit.SECONDS
        );
    }
}