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
                You are evaluating whether a job is a good match for a software engineering candidate.

                Candidate Profile:
                %s

                Job Information:
                Title: %s
                Company: %s
                Location: %s

                Job Description:
                %s

                Evaluate how well this job matches the candidate.

                Return exactly in this format:

                Score: <0-100>
                Reason: <one short sentence>
                Gap: <one short sentence>
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
                extractText(response);

        JobMatchResult result =
                parseResult(output);

        System.out.println(
                "\n===== AI JOB MATCH ====="
        );

        System.out.println(
                "Job: "
                        + job.getTitle()
                        + " @ "
                        + job.getCompany()
        );

        System.out.println(result);

        System.out.println(
                "========================\n"
        );

        return result;
    }

    private String extractText(Response response) {

        String responseString =
                response.toString();

        int textStart =
                responseString.indexOf("text=");

        if (textStart == -1) {
            throw new RuntimeException(
                    "Could not find AI response text."
            );
        }

        textStart += 5;

        int textEnd =
                responseString.indexOf(
                        ", type=output_text",
                        textStart
                );

        if (textEnd == -1) {
            throw new RuntimeException(
                    "Could not parse AI response text."
            );
        }

        return responseString
                .substring(
                        textStart,
                        textEnd
                )
                .trim();
    }

    private JobMatchResult parseResult(
            String output
    ) {

        int score = 0;
        String reason = "";
        String gap = "";

        String[] lines =
                output.split("\\R");

        for (String line : lines) {

            line = line.trim();

            if (line.startsWith("Score:")) {

                String scoreText =
                        line.substring(
                                "Score:".length()
                        ).trim();

                score =
                        Integer.parseInt(
                                scoreText
                        );

            } else if (line.startsWith("Reason:")) {

                reason =
                        line.substring(
                                "Reason:".length()
                        ).trim();

            } else if (line.startsWith("Gap:")) {

                gap =
                        line.substring(
                                "Gap:".length()
                        ).trim();
            }
        }

        return new JobMatchResult(
                score,
                reason,
                gap
        );
    }
}