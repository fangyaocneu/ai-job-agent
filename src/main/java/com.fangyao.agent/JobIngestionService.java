package com.fangyao.agent;

import java.util.ArrayList;
import java.util.List;

public class JobIngestionService {

    private final List<JobSource> jobSources;

    public JobIngestionService(List<JobSource> jobSources) {
        this.jobSources = jobSources;
    }

    public List<Job> fetchAllJobs() {

        List<Job> allNewJobs = new ArrayList<>();

        for (JobSource jobSource : jobSources) {

            try {

                System.out.println(
                        "[JobIngestion] Fetching from: "
                                + jobSource.getName()
                );

                List<Job> jobs =
                        jobSource.fetchJobs();

                if (jobs.isEmpty()) {

                    System.out.println(
                            "["
                                    + jobSource.getName()
                                    + "] No new jobs found."
                    );

                    continue;
                }

                System.out.println(
                        "["
                                + jobSource.getName()
                                + "] Found "
                                + jobs.size()
                                + " new jobs."
                );

                allNewJobs.addAll(jobs);

            } catch (Exception e) {

                System.err.println(
                        "[JobIngestion] Failed source: "
                                + jobSource.getName()
                );

                e.printStackTrace();
            }
        }

        return allNewJobs;
    }
}