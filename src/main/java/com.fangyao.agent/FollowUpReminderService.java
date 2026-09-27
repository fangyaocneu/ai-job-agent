package com.fangyao.agent;

import java.util.List;

public class FollowUpReminderService {

    private final JobRepository jobRepository;

    public FollowUpReminderService() {
        this.jobRepository = new JobRepository();
    }

    public String buildReminderEmail() {

        List<Job> jobs = jobRepository.getFollowUpsDue();

        if (jobs.isEmpty()) {
            return null;
        }

        StringBuilder body = new StringBuilder();

        body.append("You have ")
                .append(jobs.size())
                .append(" application");

        if (jobs.size() != 1) {
            body.append("s");
        }

        body.append(" to follow up:\n\n");

        for (Job job : jobs) {

            body.append(job.getCompany() != null
                    ? job.getCompany()
                    : "Unknown Company");

            body.append(" — ");

            body.append(job.getTitle() != null
                    ? job.getTitle()
                    : "Unknown Role");

            body.append("\n");

            body.append("Stage: ")
                    .append(
                            job.getApplicationStage() != null
                                    ? job.getApplicationStage()
                                    : "UNKNOWN"
                    )
                    .append("\n");

            body.append("Follow-up date: ")
                    .append(
                            job.getFollowUpDate() != null
                                    ? job.getFollowUpDate()
                                    : "Not specified"
                    )
                    .append("\n");

            if (job.getUrl() != null) {
                body.append("Job: ")
                        .append(job.getUrl())
                        .append("\n");
            }

            body.append("\n");
        }

        return body.toString();
    }
}