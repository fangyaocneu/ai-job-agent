package com.fangyao.agent;

import java.util.List;

public class JobEmailWorkflow {

    private final SentJobRepository sentJobRepository;
    private final EmailService emailService;

    public JobEmailWorkflow(
            SentJobRepository sentJobRepository,
            EmailService emailService
    ) {
        this.sentJobRepository = sentJobRepository;
        this.emailService = emailService;
    }

    public void run(String recipientEmail) {

        List<Job> unsentJobs =
                sentJobRepository.getUnsentJobs();

        if (unsentJobs.isEmpty()) {

            System.out.println(
                    "[Job Email] No high-match unsent jobs found."
            );

            return;
        }

        StringBuilder body =
                new StringBuilder();

        body.append("Daily AI Job Matches\n\n");

        for (Job job : unsentJobs) {

            body.append("==============================\n");

            body.append(job.getTitle())
                    .append("\n");

            body.append("Company: ")
                    .append(job.getCompany())
                    .append("\n");

            body.append("Location: ")
                    .append(job.getLocation())
                    .append("\n");

            body.append("Match Score: ")
                    .append(job.getMatchScore())
                    .append("/100\n");

            body.append("Reason: ")
                    .append(job.getMatchReason())
                    .append("\n");

            body.append("Gap: ")
                    .append(job.getMatchGap())
                    .append("\n");

            body.append("URL: ")
                    .append(job.getUrl())
                    .append("\n\n");
        }

        boolean sent =
                emailService.sendEmail(
                        recipientEmail,
                        "Daily AI Job Agent Update",
                        body.toString()
                );

        if (!sent) {

            System.out.println(
                    "[Job Email] Email failed. Jobs were NOT marked as sent."
            );

            return;
        }

        for (Job job : unsentJobs) {

            sentJobRepository.markAsSent(
                    job.getId()
            );
        }

        System.out.println(
                "[Job Email] "
                        + unsentJobs.size()
                        + " high-match jobs sent and marked as sent."
        );
    }
}