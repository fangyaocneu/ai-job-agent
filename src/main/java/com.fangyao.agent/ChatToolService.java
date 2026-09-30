package com.fangyao.agent;

import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;

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

        List<Job> jobs
                = jobRepository.getHighMatchJobs();

        if (jobs == null
                || jobs.isEmpty()) {

            return "No high-match jobs are currently available.";
        }

        int safeLimit
                = Math.max(
                        1,
                        Math.min(
                                requestedLimit,
                                20
                        )
                );

        int actualLimit
                = Math.min(
                        jobs.size(),
                        safeLimit
                );

        StringBuilder result
                = new StringBuilder();

        result.append(
                "Current top job matches:\n\n"
        );

        for (int i = 0;
                i < actualLimit;
                i++) {

            Job job
                    = jobs.get(i);

            Integer displayedScore
                    = job.getFinalScore() != null
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

        Integer displayedFinalScore
                = job.getFinalScore() != null
                ? job.getFinalScore()
                : job.getMatchScore();

        String skillScore
                = job.getSkillScore() != null
                ? job.getSkillScore().toString()
                : "Not available";

        String experienceScore
                = job.getExperienceScore() != null
                ? job.getExperienceScore().toString()
                : "Not available";

        String roleFitScore
                = job.getRoleFitScore() != null
                ? job.getRoleFitScore().toString()
                : "Not available";

        String preferenceScore
                = job.getPreferenceScore() != null
                ? job.getPreferenceScore().toString()
                : "Not available";

        String matchReason
                = job.getMatchReason() != null
                ? job.getMatchReason()
                : "Not available";

        String matchGap
                = job.getMatchGap() != null
                ? job.getMatchGap()
                : "Not available";

        StringBuilder result
                = new StringBuilder();

        result.append(
                "Job details from the database:\n\n"
        );

        result.append("Job ID: ");
        result.append(job.getId());
        result.append("\n");

        result.append("Title: ");
        result.append(job.getTitle());
        result.append("\n");

        result.append("Company: ");
        result.append(job.getCompany());
        result.append("\n");

        result.append("Location: ");
        result.append(job.getLocation());
        result.append("\n\n");

        result.append("Final Match Score: ");
        result.append(
                displayedFinalScore != null
                        ? displayedFinalScore
                        : "Not available"
        );
        result.append("\n");

        result.append("Skill Score: ");
        result.append(skillScore);
        result.append("\n");

        result.append("Experience Score: ");
        result.append(experienceScore);
        result.append("\n");

        result.append("Role Fit Score: ");
        result.append(roleFitScore);
        result.append("\n");

        result.append("Preference Score: ");
        result.append(preferenceScore);
        result.append("\n\n");

        result.append("Match Reason: ");
        result.append(matchReason);
        result.append("\n");

        result.append("Skill Gap: ");
        result.append(matchGap);
        result.append("\n");

        return result.toString();
    }

    // =========================
    // Tool 3: Follow-Ups
    // =========================
    public String getFollowUps(
            String requestedScope
    ) {

        List<Job> jobs
                = jobRepository.getAllJobs();

        if (jobs == null || jobs.isEmpty()) {
            return "There are currently no tracked jobs.";
        }

        String scope
                = requestedScope == null
                        ? "DUE"
                        : requestedScope
                                .trim()
                                .toUpperCase();

        LocalDate today
                = LocalDate.now();

        List<Job> filteredJobs
                = jobs.stream()
                        .filter(Job::isApplied)
                        .filter(job -> job.getFollowUpDate() != null)
                        .filter(job -> {

                            LocalDate followUpDate
                                    = job.getFollowUpDate();

                            return switch (scope) {

                                case "OVERDUE" ->
                                    followUpDate.isBefore(today);

                                case "TODAY" ->
                                    followUpDate.isEqual(today);

                                case "ALL" ->
                                    true;

                                case "DUE" ->
                                    !followUpDate.isAfter(today);

                                default ->
                                    !followUpDate.isAfter(today);
                            };
                        })
                        .toList();

        if (filteredJobs.isEmpty()) {

            return switch (scope) {

                case "OVERDUE" ->
                    "There are currently no overdue follow-ups.";

                case "TODAY" ->
                    "There are no follow-ups due today.";

                case "ALL" ->
                    "There are currently no scheduled follow-ups.";

                default ->
                    "There are currently no follow-ups due.";
            };
        }

        StringBuilder result
                = new StringBuilder();

        result.append(
                switch (scope) {

            case "OVERDUE" ->
                "Overdue follow-ups:\n\n";

            case "TODAY" ->
                "Follow-ups due today:\n\n";

            case "ALL" ->
                "All scheduled follow-ups:\n\n";

            default ->
                "Applications with follow-ups due:\n\n";
        }
        );

        for (int i = 0;
                i < filteredJobs.size();
                i++) {

            Job job
                    = filteredJobs.get(i);

            Integer displayedScore
                    = job.getFinalScore() != null
                    ? job.getFinalScore()
                    : job.getMatchScore();

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

            result.append("Follow-up Date: ");
            result.append(job.getFollowUpDate());
            result.append("\n");

            result.append("Match Score: ");
            result.append(displayedScore);
            result.append("\n\n");
        }

        return result.toString();
    }
    // =========================
// Tool 4: Applications
// =========================

    public String getApplications(
            String requestedStage
    ) {

        List<Job> allJobs
                = jobRepository.getAllJobs();

        if (allJobs == null
                || allJobs.isEmpty()) {

            return "No applications are currently being tracked.";
        }

        String stage
                = requestedStage == null
                        ? "ALL"
                        : requestedStage
                                .trim()
                                .toUpperCase();

        List<Job> jobs
                = allJobs.stream()
                        .filter(
                                Job::isApplied
                        )
                        .filter(
                                job
                                -> stage.equals("ALL")
                                || (job.getApplicationStage() != null
                                && job.getApplicationStage()
                                        .equalsIgnoreCase(
                                                stage
                                        ))
                        )
                        .toList();

        if (jobs.isEmpty()) {

            return stage.equals("ALL")
                    ? "No applications are currently being tracked."
                    : "No tracked applications were found in stage: "
                    + stage;
        }

        StringBuilder result
                = new StringBuilder();

        result.append(
                stage.equals("ALL")
                ? "Tracked applications:\n\n"
                : "Tracked applications in stage "
                + stage
                + ":\n\n"
        );

        for (Job job
                : jobs) {

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
                    "Stage: "
            );

            result.append(
                    job.getApplicationStage()
            );

            result.append("\n");

            result.append(
                    "Applied At: "
            );

            result.append(
                    job.getAppliedAt()
            );

            result.append("\n");

            result.append(
                    "Follow-up Date: "
            );

            result.append(
                    job.getFollowUpDate()
            );

            result.append("\n");

            Integer displayedScore
                    = job.getFinalScore() != null
                    ? job.getFinalScore()
                    : job.getMatchScore();

            result.append(
                    "Match Score: "
            );

            result.append(
                    displayedScore
            );

            result.append("\n\n");
        }

        return result.toString();
    }
// =========================
// Tool 5: Candidate Profile
// =========================

    public String getCandidateProfile(
            String requestedSection
    ) {

        CandidateProfile profile
                = profileRepository.loadProfile();

        if (profile == null) {
            return "No candidate profile is currently available.";
        }

        String section
                = requestedSection == null
                        ? "ALL"
                        : requestedSection
                                .trim()
                                .toUpperCase();

        return switch (section) {

            case "SKILLS" ->
                """
                Candidate skills:

                Languages: %s
                Backend: %s
                Cloud: %s
                Databases: %s
                Frontend: %s
                Tools: %s
                """.formatted(
                profile.getLanguages(),
                profile.getBackendTechnologies(),
                profile.getCloudTechnologies(),
                profile.getDatabases(),
                profile.getFrontendTechnologies(),
                profile.getTools()
                );

            case "EXPERIENCE" ->
                """
                Candidate experience:

                %s
                """.formatted(
                profile.getExperienceHighlights()
                );

            case "PROJECTS" ->
                """
                Candidate projects:

                %s
                """.formatted(
                profile.getProjectHighlights()
                );

            case "TARGET_ROLES" ->
                """
                Candidate target roles:

                %s
                """.formatted(
                profile.getTargetRoles()
                );

            case "LOCATIONS" ->
                """
                Candidate preferred locations:

                %s

                Preferred work modes:

                %s
                """.formatted(
                profile.getPreferredLocations(),
                profile.getPreferredWorkModes()
                );

            case "ALL" ->
                """
                Current candidate profile:

                %s
                """.formatted(
                profile.getProfileSummary()
                );

            default ->
                """
                Unknown profile section: %s

                Supported sections:
                SKILLS
                EXPERIENCE
                PROJECTS
                TARGET_ROLES
                LOCATIONS
                ALL
                """.formatted(
                section
                );
        };
    }
}
