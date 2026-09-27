package com.fangyao.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class StrategyAgent
        implements Agent<AgentContext, AgentContext>, AutoCloseable {

    private final OpenAIClient client;
    private final ObjectMapper objectMapper;
    private final CandidateProfile profile;

    public StrategyAgent() {

        this.client =
                OpenAIOkHttpClient.fromEnv();

        this.objectMapper =
                new ObjectMapper();

        this.profile =
                new CandidateProfile();
    }

    @Override
    public AgentContext execute(
            AgentContext context
    ) {

        Job job =
                context.getJob();

        JobAnalysis analysis =
                context.getJobAnalysis();

        MatchEvaluation evaluation =
                context.getMatchEvaluation();

        if (evaluation == null) {

            System.err.println(
                    "[StrategyAgent] Missing match evaluation for job ID: "
                            + job.getId()
            );

            return context;
        }

        try {

            System.out.println(
                    "[StrategyAgent] Building strategy for job ID: "
                            + job.getId()
                            + " | "
                            + job.getTitle()
            );

            String prompt =
                    buildPrompt(
                            job,
                            analysis,
                            evaluation
                    );

            ResponseCreateParams request =
                    ResponseCreateParams.builder()
                            .model(ChatModel.GPT_5_2)
                            .input(prompt)
                            .build();

            Response response =
                    client.responses().create(
                            request
                    );

            String output =
                    extractOutputText(
                            response
                    );

            ApplicationStrategy strategy =
                    objectMapper.readValue(
                            output,
                            ApplicationStrategy.class
                    );

            context.setStrategy(
                    strategy
            );

            printStrategy(
                    job,
                    strategy
            );

            System.out.println(
                    "[StrategyAgent] Completed job ID: "
                            + job.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "[StrategyAgent] Failed job ID: "
                            + job.getId()
            );

            e.printStackTrace();
        }

        return context;
    }

    private String buildPrompt(
            Job job,
            JobAnalysis analysis,
            MatchEvaluation evaluation
    ) {

        String analysisSection;

        if (analysis == null) {

            analysisSection =
                    "No structured job analysis available.";

        } else {

            analysisSection = """
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

        return """
                You are an application strategy agent.

                Decide how the candidate should approach this job.

                Candidate Profile:
                %s

                Job:

                Title:
                %s

                Company:
                %s

                Location:
                %s

                Structured Job Analysis:
                %s

                Match Evaluation:

                Overall Score: %d
                Skill Score: %d
                Experience Score: %d
                Role Fit Score: %d

                Strengths:
                %s

                Missing Skills:
                %s

                Reason:
                %s

                Gap:
                %s

                Return ONLY valid JSON using exactly this structure:

                {
                  "recommendation": "APPLY | SKIP",
                  "priority": "HIGH | MEDIUM | LOW",
                  "resumeFocus": [
                    "focus 1",
                    "focus 2"
                  ],
                  "concerns": [
                    "concern 1",
                    "concern 2"
                  ],
                  "applicationAdvice": "one short practical sentence"
                }

                Decision guidance:

                HIGH:
                Strong target role and candidate has a competitive profile.

                MEDIUM:
                Worth applying, but meaningful gaps exist.

                LOW:
                Weak fit or significant mismatch.

                recommendation = APPLY when the role is reasonably worth pursuing.

                recommendation = SKIP when:
                - role type is clearly wrong
                - seniority is far beyond the candidate
                - critical requirements are substantially missing
                - the match score is very weak

                Important:
                - Use the existing match evaluation as evidence.
                - Do not invent candidate experience.
                - resumeFocus should only contain things the candidate actually has.
                - concerns should highlight realistic application risks.
                - Do not include markdown.
                - Return JSON only.
                """
                .formatted(
                        profile.getProfileSummary(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        analysisSection,
                        evaluation.getOverallScore(),
                        evaluation.getSkillScore(),
                        evaluation.getExperienceScore(),
                        evaluation.getRoleFitScore(),
                        evaluation.getStrengths(),
                        evaluation.getMissingSkills(),
                        evaluation.getReason(),
                        evaluation.getGap()
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

    private void printStrategy(
            Job job,
            ApplicationStrategy strategy
    ) {

        System.out.println();

        System.out.println(
                "===== APPLICATION STRATEGY ====="
        );

        System.out.println(
                "Job: "
                        + job.getTitle()
                        + " @ "
                        + job.getCompany()
        );

        System.out.println(
                "Recommendation: "
                        + strategy.getRecommendation()
        );

        System.out.println(
                "Priority: "
                        + strategy.getPriority()
        );

        System.out.println(
                "Resume Focus: "
                        + strategy.getResumeFocus()
        );

        System.out.println(
                "Concerns: "
                        + strategy.getConcerns()
        );

        System.out.println(
                "Advice: "
                        + strategy.getApplicationAdvice()
        );

        System.out.println(
                "================================"
        );

        System.out.println();
    }

    @Override
    public void close() {

        client.close();
    }
}