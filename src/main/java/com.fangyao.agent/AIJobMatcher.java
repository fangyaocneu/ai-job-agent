package com.fangyao.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class AIJobMatcher {

    private final OpenAIClient client;
    private final CandidateProfile profile;

    public AIJobMatcher() {
        this.client = OpenAIOkHttpClient.fromEnv();
        this.profile = new CandidateProfile();
    }

    public JobMatchResult scoreJob(Job job) {

        String prompt = """
                You are evaluating how well a job matches a software engineering candidate.

                Candidate Profile:
                %s

                Job Title:
                %s

                Company:
                %s

                Location:
                %s

                Job Description:
                %s

                Evaluate the job against the candidate profile.

                Return exactly this format:

                Score: <0-100>
                Reason: <one short sentence>
                Gap: <one short sentence>

                The score should reflect how closely the role matches the candidate's
                skills, experience, and target software engineering roles.
                """.formatted(
                profile.getProfileSummary(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription()
        );

        ResponseCreateParams params =
                ResponseCreateParams.builder()
                        .model(ChatModel.GPT_5_2)
                        .input(prompt)
                        .build();

        Response response =
                client.responses().create(params);

        String output =
                extractOutputText(response);

        JobMatchResult result =
                parseResult(output);

        System.out.println();
        System.out.println("===== AI JOB MATCH =====");

        System.out.println(
                "Job: "
                        + job.getTitle()
                        + " @ "
                        + job.getCompany()
        );

        System.out.println(
                "Score: " + result.getScore()
        );

        System.out.println(
                "Reason: " + result.getReason()
        );

        System.out.println(
                "Gap: " + result.getGap()
        );

        System.out.println();
        System.out.println("========================");
        System.out.println();

        return result;
    }

    private String extractOutputText(Response response) {

        String raw =
                response.toString();

        int start =
                raw.indexOf("text=");

        if (start == -1) {
            throw new IllegalStateException(
                    "Could not find text in OpenAI response."
            );
        }

        start += "text=".length();

        int end =
                raw.indexOf(
                        ", type=output_text",
                        start
                );

        if (end == -1) {
            throw new IllegalStateException(
                    "Could not parse OpenAI response text."
            );
        }

        return raw.substring(
                start,
                end
        ).trim();
    }

    private JobMatchResult parseResult(String output) {

        int score = 0;
        String reason = "";
        String gap = "";

        String[] lines =
                output.split("\\R");

        for (String line : lines) {

            String trimmed =
                    line.trim();

            if (trimmed.startsWith("Score:")) {

                String value =
                        trimmed
                                .substring(
                                        "Score:".length()
                                )
                                .trim();

                score =
                        Integer.parseInt(value);

            } else if (
                    trimmed.startsWith("Reason:")
            ) {

                reason =
                        trimmed
                                .substring(
                                        "Reason:".length()
                                )
                                .trim();

            } else if (
                    trimmed.startsWith("Gap:")
            ) {

                gap =
                        trimmed
                                .substring(
                                        "Gap:".length()
                                )
                                .trim();
            }
        }

        return new JobMatchResult(
                score,
                reason,
                gap
        );
    }

    public void close() {

        client.close();
    }
}