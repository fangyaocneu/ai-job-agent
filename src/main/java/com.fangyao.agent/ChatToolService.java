package com.fangyao.agent;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatToolService {

    private final JobRepository jobRepository;
    private final CandidateProfileRepository profileRepository;

    public ChatToolService() {

        this.jobRepository
                = new JobRepository();

        this.profileRepository
                = new CandidateProfileRepository();
    }

    // =========================
// Tool 1: Top Matches
// =========================
    public String getTopMatches(
        int requestedLimit
) {

    List<Job> jobs =
            jobRepository.getHighMatchJobs();

    if (
            jobs == null
                    || jobs.isEmpty()
    ) {

        return "No high-match jobs are currently available.";
    }

    int safeLimit =
            Math.max(
                    1,
                    Math.min(
                            requestedLimit,
                            20
                    )
            );

    int actualLimit =
            Math.min(
                    jobs.size(),
                    safeLimit
            );

    StringBuilder result =
            new StringBuilder();

    result.append(
            "Current top job matches:\n\n"
    );

    for (
            int i = 0;
            i < actualLimit;
            i++
    ) {

        Job job =
                jobs.get(i);

        Integer displayedScore =
                job.getFinalScore() != null
                        ? job.getFinalScore()
                        : job.getMatchScore();

        result.append(
                i + 1
        );

        result.append(". ");

        result.append(
                job.getTitle()
        );

        result.append(" @ ");

        result.append(
                job.getCompany()
        );

        result.append("\n");

        result.append(
                "Job ID: "
        );

        result.append(
                job.getId()
        );

        result.append("\n");

        result.append(
                "Location: "
        );

        result.append(
                job.getLocation()
        );

        result.append("\n");

        result.append(
                "Final Match Score: "
        );

        result.append(
                displayedScore
        );

        result.append("\n");

        result.append(
                "Reason: "
        );

        result.append(
                job.getMatchReason()
        );

        result.append("\n\n");
    }

    return result.toString();
}

    // =========================
    // Tool 2: Job Insights
    // =========================
    public String getJobInsights(
            int jobId
    ) {

        Job job
                = jobRepository.getJobById(
                        jobId
                );

        if (job == null) {

            return "No job was found with job ID "
                    + jobId
                    + ".";
        }

        StringBuilder result
                = new StringBuilder();

        result.append(
                "Job details from the database:\n\n"
        );

        result.append(
                "Job ID: "
        );

        result.append(
                job.getId()
        );

        result.append("\n");

        result.append(
                "Title: "
        );

        result.append(
                job.getTitle()
        );

        result.append("\n");

        result.append(
                "Company: "
        );

        result.append(
                job.getCompany()
        );

        result.append("\n");

        result.append(
                "Location: "
        );

        result.append(
                job.getLocation()
        );

        result.append("\n\n");

        result.append(
                "Final Match Score: "
        );

        result.append(
                job.getFinalScore()
        );

        result.append("\n");

        result.append(
                "Skill Score: "
        );

        result.append(
                job.getSkillScore()
        );

        result.append("\n");

        result.append(
                "Experience Score: "
        );

        result.append(
                job.getExperienceScore()
        );

        result.append("\n");

        result.append(
                "Role Fit Score: "
        );

        result.append(
                job.getRoleFitScore()
        );

        result.append("\n");

        result.append(
                "Preference Score: "
        );

        result.append(
                job.getPreferenceScore()
        );

        result.append("\n\n");

        result.append(
                "Match Reason: "
        );

        result.append(
                job.getMatchReason()
        );

        result.append("\n");

        result.append(
                "Skill Gap: "
        );

        result.append(
                job.getMatchGap()
        );

        result.append("\n");

        return result.toString();
    }

    // =========================
    // Tool 3: Follow-Ups
    // =========================
    public String getFollowUps() {

        List<Job> jobs
                = jobRepository.getFollowUpsDue();

        if (jobs == null
                || jobs.isEmpty()) {

            return "There are currently no follow-ups due.";
        }

        StringBuilder result
                = new StringBuilder();

        result.append(
                "Applications with follow-ups due:\n\n"
        );

        for (int i = 0;
                i < jobs.size();
                i++) {

            Job job
                    = jobs.get(i);

            result.append(
                    i + 1
            );

            result.append(". ");

            result.append(
                    job.getTitle()
            );

            result.append(" @ ");

            result.append(
                    job.getCompany()
            );

            result.append("\n");

            result.append(
                    "Job ID: "
            );

            result.append(
                    job.getId()
            );

            result.append("\n");

            result.append(
                    "Application Stage: "
            );

            result.append(
                    job.getApplicationStage()
            );

            result.append("\n");

            result.append(
                    "Follow-up Date: "
            );

            result.append(
                    job.getFollowUpDate()
            );

            result.append("\n");

            result.append(
                    "Match Score: "
            );

            result.append(
                    job.getMatchScore()
            );

            result.append("\n\n");
        }

        return result.toString();
    }
    // =========================
// Tool 4: Applications
// =========================

    public String getApplications() {

        List<Job> allJobs
                = jobRepository.getAllJobs();

        List<Job> jobs
                = allJobs.stream()
                        .filter(Job::isApplied)
                        .toList();

        if (jobs.isEmpty()) {

            return "There are currently no tracked applications.";
        }

        StringBuilder result
                = new StringBuilder();

        result.append(
                "Current tracked applications:\n\n"
        );

        for (int i = 0;
                i < jobs.size();
                i++) {

            Job job
                    = jobs.get(i);

            result.append(i + 1);
            result.append(". ");
            result.append(job.getTitle());
            result.append(" @ ");
            result.append(job.getCompany());
            result.append("\n");

            result.append("Job ID: ");
            result.append(job.getId());
            result.append("\n");

            result.append("Application Stage: ");
            result.append(job.getApplicationStage());
            result.append("\n");

            result.append("Applied Date: ");
            result.append(job.getAppliedAt());
            result.append("\n");

            result.append("Follow-up Date: ");
            result.append(job.getFollowUpDate());
            result.append("\n");

            result.append("Match Score: ");
            result.append(job.getMatchScore());
            result.append("\n\n");
        }

        return result.toString();
    }
// =========================
// Tool 5: Candidate Profile
// =========================

    public String getCandidateProfile() {

        CandidateProfile profile
                = profileRepository.loadProfile();

        if (profile == null) {

            return "No candidate profile is currently available.";
        }

        return """
            Current candidate profile:

            %s
            """.formatted(
                profile.getProfileSummary()
        );
    }
}
