package com.fangyao.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JevClient {

    private static final String ENDPOINT =
            "https://jevmodel.org/v1/systemone";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JevClient() {

        this.apiKey =
                System.getenv("JEVMODEL_API_KEY");

        if (apiKey == null
                || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "JEVMODEL_API_KEY is not set."
            );
        }

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(10)
                        )
                        .build();

        this.objectMapper =
                new ObjectMapper();
    }

    public JevDecision classifyIntent(
            String message
    ) {

        try {

            Map<String, Object> criteria =
                    new LinkedHashMap<>();

            criteria.put(
                    "RESUME",
                    "Questions about the candidate's resume, skills, experience, education, or resume improvement."
            );

            criteria.put(
                    "JOB_SEARCH",
                    "Requests to find, search, browse, or recommend job opportunities."
            );

            criteria.put(
                    "JOB_MATCH",
                    "Questions about how well the candidate matches a specific job or job description."
            );

            criteria.put(
                    "APPLICATION",
                    "Questions about applications, application status, follow-ups, tracking, or application workflow."
            );

            criteria.put(
                    "GENERAL",
                    "General career questions that do not clearly belong to the other categories."
            );

            Map<String, Object> routeQuestion =
                    new LinkedHashMap<>();

            routeQuestion.put(
                    "type",
                    "choice"
            );

            routeQuestion.put(
                    "instructions",
                    "Classify the user's message into the single best intent for an AI career assistant."
            );

            routeQuestion.put(
                    "criteria",
                    criteria
            );

            Map<String, Object> questions =
                    new LinkedHashMap<>();

            questions.put(
                    "route",
                    routeQuestion
            );

            Map<String, Object> body =
                    new LinkedHashMap<>();

            body.put(
                    "model",
                    "jev-latest"
            );

            body.put(
                    "state",
                    message
            );

            body.put(
                    "questions",
                    questions
            );

            String jsonBody =
                    objectMapper.writeValueAsString(
                            body
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            ENDPOINT
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(20)
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            jsonBody
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                throw new RuntimeException(
                        "Jev API failed. HTTP "
                        + response.statusCode()
                        + ": "
                        + response.body()
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode route =
                    root.path(
                            "answers"
                    ).path(
                            "route"
                    );

            String choice =
                    route.path(
                            "choice"
                    ).asText();

            double confidence =
                    route.path(
                            "confidence"
                    ).asDouble();

            return new JevDecision(
                    choice,
                    confidence
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to classify intent with Jev.",
                    e
            );
        }
    }

    public static class JevDecision {

        private final String choice;
        private final double confidence;

        public JevDecision(
                String choice,
                double confidence
        ) {

            this.choice =
                    choice;

            this.confidence =
                    confidence;
        }

        public String getChoice() {
            return choice;
        }

        public double getConfidence() {
            return confidence;
        }
    }
}