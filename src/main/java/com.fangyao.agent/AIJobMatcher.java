package com.fangyao.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class AIJobMatcher {

    private final OpenAIClient client;
    private final CandidateProfile profile;
    private final ObjectMapper objectMapper;

    public AIJobMatcher(
            OpenAIClient client
    ) {

        this.client =
                client;

        CandidateProfileRepository profileRepository =
                new CandidateProfileRepository();

        this.profile =
                profileRepository.loadProfile();

        if (this.profile == null) {

            throw new IllegalStateException(
                    "Candidate profile not found in database."
            );
        }

        this.objectMapper =
                new ObjectMapper();

        System.out.println(
                "[AIJobMatcher] Loaded candidate profile from database: "
                        + profile.getName()
        );
    }

    // ==========================
    // Backward-compatible
    // ==========================

    public JobMatchResult scoreJob(
            Job job
    ) {

        MatchEvaluation evaluation =
                evaluateJob(
                        job,
                        null
                );

        return evaluation.toJobMatchResult();
    }

    // ==========================
    // Structured analysis support
    // ==========================

    public JobMatchResult scoreJob(
            Job job,
            JobAnalysis analysis
    ) {

        MatchEvaluation evaluation =
                evaluateJob(
                        job,
                        analysis
                );

        return evaluation.toJobMatchResult();
    }

    // ==========================
    // Phase 6 personalized scoring
    // ==========================

    public MatchEvaluation evaluateJob(
            Job job,
            JobAnalysis analysis
    ) {

        String analysisSection =
                buildAnalysisSection(
                        analysis
                );

        String prompt = """
                You are evaluating how well a software engineering job
                matches a candidate.

                Candidate Profile:

                Languages:
                %s

                Backend:
                %s

                Cloud:
                %s

                Databases:
                %s

                Frontend:
                %s

                Tools:
                %s

                Target Roles:
                %s

                Experience:
                %s

                Projects:
                %s

                Preferred Locations:
                %s

                Preferred Work Modes:
                %s

                Job Information:

                Title:
                %s

                Company:
                %s

                Location:
                %s

                Structured Job Analysis:

                %s

                Original Job Description:

                %s

                Evaluate the candidate against this role.

                Return ONLY valid JSON in exactly this structure:

                {
                  "overallScore": 0,
                  "skillScore": 0,
                  "experienceScore": 0,
                  "roleFitScore": 0,
                  "preferenceScore": 0,
                  "reason": "one short sentence",
                  "gap": "one short sentence",
                  "strengths": [
                    "strength 1",
                    "strength 2"
                  ],
                  "missingSkills": [
                    "missing skill 1",
                    "missing skill 2"
                  ]
                }

                Scoring rules:

                skillScore:
                Score from 0 to 100.
                Measure how closely the required technologies and skills
                match the candidate's actual technical skills.

                experienceScore:
                Score from 0 to 100.
                Measure how closely the required experience level,
                responsibilities, and background match the candidate.

                roleFitScore:
                Score from 0 to 100.
                Measure how closely the role type and seniority
                match the candidate's target roles.

                preferenceScore:
                Score from 0 to 100.
                Measure how well the job matches the candidate's
                preferred locations and preferred work modes.

                For preferenceScore:
                - Strong location and work-mode match should score high.
                - Partial match should score moderately.
                - Clear conflict with candidate preferences should score low.
                - If the job description does not clearly specify work mode,
                  do not automatically give a low score.
                - If location information is incomplete, use a neutral score
                  rather than inventing information.

                overallScore:
                Give a holistic score from 0 to 100.
                This value is informational only.
                The application will calculate the final weighted score itself.

                strengths:
                List the candidate's strongest relevant qualifications.

                missingSkills:
                List important required skills that are missing or unclear.

                Score interpretation:

                90-100 = exceptional match
                75-89 = strong match
                60-74 = moderate match
                40-59 = weak match
                0-39 = poor match

                Important:
                - Use the structured job analysis as the primary interpretation.
                - Use the original description as supporting evidence.
                - Use the candidate profile as the only source of candidate skills.
                - Use preferred locations and work modes when calculating preferenceScore.
                - Do not invent candidate experience.
                - Do not invent job requirements.
                - Do not include markdown.
                - Do not include ```json.
                - Return JSON only.
                """
                .formatted(
                        profile.getLanguages(),
                        profile.getBackendTechnologies(),
                        profile.getCloudTechnologies(),
                        profile.getDatabases(),
                        profile.getFrontendTechnologies(),
                        profile.getTools(),
                        profile.getTargetRoles(),
                        profile.getExperienceHighlights(),
                        profile.getProjectHighlights(),
                        profile.getPreferredLocations(),
                        profile.getPreferredWorkModes(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        analysisSection,
                        job.getDescription()
                );

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .model(
                                ChatModel.GPT_5_2
                        )
                        .input(
                                prompt
                        )
                        .build();

        Response response =
                client.responses()
                        .create(
                                params
                        );

        String output =
                extractOutputText(
                        response
                );

        MatchEvaluation evaluation =
                parseEvaluation(
                        output
                );

        normalizeScores(
                evaluation
        );

        int finalScore =
                calculateFinalScore(
                        evaluation
                );

        evaluation.setFinalScore(
                finalScore
        );

        printEvaluation(
                job,
                analysis,
                evaluation
        );

        return evaluation;
    }

    // ==========================
    // Weighted scoring
    // ==========================

    private int calculateFinalScore(
            MatchEvaluation evaluation
    ) {

        double weightedScore =
                evaluation.getSkillScore()
                        * 0.40
                        +
                evaluation.getExperienceScore()
                        * 0.25
                        +
                evaluation.getRoleFitScore()
                        * 0.20
                        +
                evaluation.getPreferenceScore()
                        * 0.15;

        return (int) Math.round(
                weightedScore
        );
    }

    // ==========================
    // Score safety
    // ==========================

    private void normalizeScores(
            MatchEvaluation evaluation
    ) {

        evaluation.setOverallScore(
                clampScore(
                        evaluation.getOverallScore()
                )
        );

        evaluation.setSkillScore(
                clampScore(
                        evaluation.getSkillScore()
                )
        );

        evaluation.setExperienceScore(
                clampScore(
                        evaluation.getExperienceScore()
                )
        );

        evaluation.setRoleFitScore(
                clampScore(
                        evaluation.getRoleFitScore()
                )
        );

        evaluation.setPreferenceScore(
                clampScore(
                        evaluation.getPreferenceScore()
                )
        );
    }

    private int clampScore(
            int score
    ) {

        return Math.max(
                0,
                Math.min(
                        100,
                        score
                )
        );
    }

    // ==========================
    // Job analysis section
    // ==========================

    private String buildAnalysisSection(
            JobAnalysis analysis
    ) {

        if (analysis == null) {

            return """
                    No structured job analysis is available.
                    Use the original job description.
                    """;
        }

        return """
                Role Type: %s
                Seniority: %s
                Primary Skills: %s
                Secondary Skills: %s
                Required Years Experience: %s
                Summary: %s
                """
                .formatted(
                        analysis.getRoleType(),
                        analysis.getSeniority(),
                        analysis.getPrimarySkills(),
                        analysis.getSecondarySkills(),
                        analysis.getRequiredYearsExperience(),
                        analysis.getSummary()
                );
    }

    // ==========================
    // OpenAI response extraction
    // ==========================

    private String extractOutputText(
            Response response
    ) {

        StringBuilder output =
                new StringBuilder();

        response.output()
                .stream()
                .flatMap(
                        item ->
                                item.message()
                                        .stream()
                )
                .flatMap(
                        message ->
                                message.content()
                                        .stream()
                )
                .flatMap(
                        content ->
                                content.outputText()
                                        .stream()
                )
                .forEach(
                        text ->
                                output.append(
                                        text.text()
                                )
                );

        if (output.isEmpty()) {

            throw new IllegalStateException(
                    "Could not find text in OpenAI response."
            );
        }

        return output.toString()
                .trim();
    }

    // ==========================
    // JSON parsing
    // ==========================

    private MatchEvaluation parseEvaluation(
            String output
    ) {

        try {

            return objectMapper.readValue(
                    output,
                    MatchEvaluation.class
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Could not parse match evaluation JSON: "
                            + output,
                    e
            );
        }
    }

    // ==========================
    // Logging
    // ==========================

    private void printEvaluation(
            Job job,
            JobAnalysis analysis,
            MatchEvaluation evaluation
    ) {

        System.out.println();

        System.out.println(
                "===== AI JOB MATCH ====="
        );

        System.out.println(
                "Job: "
                        + job.getTitle()
                        + " @ "
                        + job.getCompany()
        );

        if (analysis != null) {

            System.out.println(
                    "Role Type: "
                            + analysis.getRoleType()
            );

            System.out.println(
                    "Seniority: "
                            + analysis.getSeniority()
            );
        }

        System.out.println(
                "AI Overall Score: "
                        + evaluation.getOverallScore()
        );

        System.out.println(
                "Skill Score: "
                        + evaluation.getSkillScore()
                        + " (40%)"
        );

        System.out.println(
                "Experience Score: "
                        + evaluation.getExperienceScore()
                        + " (25%)"
        );

        System.out.println(
                "Role Fit Score: "
                        + evaluation.getRoleFitScore()
                        + " (20%)"
        );

        System.out.println(
                "Preference Score: "
                        + evaluation.getPreferenceScore()
                        + " (15%)"
        );

        System.out.println(
                "FINAL WEIGHTED SCORE: "
                        + evaluation.getFinalScore()
        );

        System.out.println(
                "Strengths: "
                        + evaluation.getStrengths()
        );

        System.out.println(
                "Missing Skills: "
                        + evaluation.getMissingSkills()
        );

        System.out.println(
                "Reason: "
                        + evaluation.getReason()
        );

        System.out.println(
                "Gap: "
                        + evaluation.getGap()
        );

        System.out.println();

        System.out.println(
                "========================"
        );

        System.out.println();
    }
}