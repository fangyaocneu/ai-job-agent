package com.fangyao.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class RescoreSingleJob {

    public static void main(
            String[] args
    ) {

        int jobId =
                5162;

        System.out.println(
                "===== Rescore Single Job Started ====="
        );

        System.out.println(
                "Target Job ID: "
                        + jobId
        );

        JobRepository jobRepository =
                new JobRepository();

        JobAgentResultRepository resultRepository =
                new JobAgentResultRepository();

        OpenAIClient client =
                OpenAIOkHttpClient.fromEnv();

        AIJobMatcher matcher =
                new AIJobMatcher(
                        client
                );

        // =========================
        // Load Job
        // =========================

        Job job =
                jobRepository.getJobById(
                        jobId
                );

        if (
                job == null
        ) {

            throw new IllegalStateException(
                    "Job not found: "
                            + jobId
            );
        }

        System.out.println(
                "Job: "
                        + job.getTitle()
                        + " @ "
                        + job.getCompany()
        );

        // =========================
        // Run Latest Scoring
        // =========================

        MatchEvaluation evaluation =
                matcher.evaluateJob(
                        job,
                        null
                );

        System.out.println(
                "===== New Evaluation ====="
        );

        System.out.println(
                "Overall Score: "
                        + evaluation.getOverallScore()
        );

        System.out.println(
                "Skill Score: "
                        + evaluation.getSkillScore()
        );

        System.out.println(
                "Experience Score: "
                        + evaluation.getExperienceScore()
        );

        System.out.println(
                "Role Fit Score: "
                        + evaluation.getRoleFitScore()
        );

        System.out.println(
                "Preference Score: "
                        + evaluation.getPreferenceScore()
        );

        System.out.println(
                "Final Score: "
                        + evaluation.getFinalScore()
        );

        // =========================
        // Update Scores Only
        // =========================

        resultRepository.updateScoresOnly(
                jobId,
                evaluation
        );

        System.out.println(
                "===== Rescore Complete ====="
        );

        System.out.println(
                "Job "
                        + jobId
                        + " has been rescored successfully."
        );
    }
}