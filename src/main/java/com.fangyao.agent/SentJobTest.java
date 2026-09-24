package com.fangyao.agent;

import java.util.List;

public class SentJobTest {

    public static void main(String[] args) {

        SentJobRepository repository =
                new SentJobRepository();

        List<Job> jobs =
                repository.getUnsentJobs();

        System.out.println(
                "Unsent jobs count: " + jobs.size()
        );

        for (Job job : jobs) {

            System.out.println(
                    job.getId()
                    + " | "
                    + job.getTitle()
                    + " | "
                    + job.getCompany()
            );
        }
    }
}