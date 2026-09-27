package com.fangyao.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class JobAnalysisAgent
        implements Agent<AgentContext, AgentContext>, AutoCloseable {

    private final OpenAIClient client;
    private final ObjectMapper objectMapper;

    public JobAnalysisAgent() {
        this.client = OpenAIOkHttpClient.fromEnv();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AgentContext execute(
            AgentContext context
    ) {

        Job job = context.getJob();

        try {

            System.out.println(
                    "[JobAnalysisAgent] Analyzing job ID: "
                            + job.getId()
                            + " | "
                            + job.getTitle()
            );

            String prompt = """
                    Analyze this software engineering job posting.

                    Return ONLY valid JSON using exactly this structure:

                    {
                      "roleType": "BACKEND | FRONTEND | FULL_STACK | DEVOPS | DATA | MOBILE | OTHER",
                      "seniority": "ENTRY_LEVEL | MID_LEVEL | SENIOR | STAFF | UNKNOWN",
                      "primarySkills": ["skill1", "skill2"],
                      "secondarySkills": ["skill1", "skill2"],
                      "requiredYearsExperience": 0,
                      "summary": "short summary"
                    }

                    Job Title:
                    %s

                    Company:
                    %s

                    Location:
                    %s

                    Description:
                    %s

                    Rules:
                    - Do not include markdown.
                    - Return JSON only.
                    - Use UNKNOWN if seniority is unclear.
                    - Use 0 if required years of experience are not stated.
                    - Keep the summary concise.
                    """
                    .formatted(
                            job.getTitle(),
                            job.getCompany(),
                            job.getLocation(),
                            job.getDescription()
                    );

            ResponseCreateParams request =
                    ResponseCreateParams.builder()
                            .model("gpt-5.6-luna")
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

            JobAnalysis analysis =
                    objectMapper.readValue(
                            output,
                            JobAnalysis.class
                    );

            context.setJobAnalysis(
                    analysis
            );

            System.out.println(
                    "[JobAnalysisAgent] Completed job ID: "
                            + job.getId()
            );

        } catch (Exception e) {

            System.err.println(
                    "[JobAnalysisAgent] Failed job ID: "
                            + job.getId()
            );

            e.printStackTrace();
        }

        return context;
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

    @Override
    public void close() {
        client.close();
    }
}