package com.fangyao.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class AIJobMatcher {

    private final OpenAIClient client;
    private final CandidateProfile profile;
    private final ObjectMapper objectMapper;

    public AIJobMatcher() {

        this.client =
                OpenAIOkHttpClient.fromEnv();

        this.profile =
                new CandidateProfile();

        this.objectMapper =
                new ObjectMapper();
    }

    // Backward-compatible
    public JobMatchResult scoreJob(Job job) {

        MatchEvaluation evaluation =
                evaluateJob(
                        job,
                        null
                );

        return evaluation.toJobMatchResult();
    }

    // Backward-compatible with structured analysis
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

    // Phase 5 structured scoring
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

                overallScore:
                Overall candidate-job match from 0 to 100.

                skillScore:
                Match between required technologies and candidate skills.

                experienceScore:
                Match between required experience and candidate experience.

                roleFitScore:
                Match between the job's role type/seniority
                and the candidate's target roles.

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
                - Do not invent candidate experience.
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
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        analysisSection,
                        job.getDescription()
                );

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .model(ChatModel.GPT_5_2)
                        .input(prompt)
                        .build();

        Response response =
                client.responses().create(
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

        printEvaluation(
                job,
                analysis,
                evaluation
        );

        return evaluation;
    }

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

    private String extractOutputText(
            Response response
    ) {

        StringBuilder output =
                new StringBuilder();

        response.output().stream()
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

    public void close() {

        client.close();
    }
}