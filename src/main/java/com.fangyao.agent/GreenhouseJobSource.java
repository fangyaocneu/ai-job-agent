package com.fangyao.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class GreenhouseJobSource implements JobSource {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JobRepository repository;

    public GreenhouseJobSource() {

        this.httpClient
                = HttpClient.newHttpClient();

        this.objectMapper
                = new ObjectMapper();

        this.repository
                = new JobRepository();
    }

    @Override
    public String getName() {
        return "Greenhouse";
    }

    @Override
    public List<Job> fetchJobs() {

        List<Job> newJobs
                = new ArrayList<>();

        List<String> companyBoards
                = List.of(
                        "stripe",
                        "airbnb"
                );

        for (String board : companyBoards) {

            try {

                List<Job> jobs
                        = fetchCompanyJobs(
                                board
                        );

                newJobs.addAll(
                        jobs
                );

            } catch (Exception e) {

                System.err.println(
                        "[Greenhouse] Failed board: "
                        + board
                );

                e.printStackTrace();
            }
        }

        return newJobs;
    }

    private List<Job> fetchCompanyJobs(
            String board
    ) throws Exception {

        List<Job> newJobs
                = new ArrayList<>();

        String url
                = "https://boards-api.greenhouse.io/v1/boards/"
                + board
                + "/jobs?content=true";

        HttpRequest request
                = HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        url
                                )
                        )
                        .header(
                                "User-Agent",
                                "Mozilla/5.0"
                        )
                        .GET()
                        .build();

        HttpResponse<String> response
                = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {

            System.err.println(
                    "[Greenhouse] HTTP "
                    + response.statusCode()
                    + " for board "
                    + board
            );

            return newJobs;
        }

        JsonNode root
                = objectMapper.readTree(
                        response.body()
                );

        JsonNode jobs
                = root.get(
                        "jobs"
                );

        if (jobs == null
                || !jobs.isArray()) {

            return newJobs;
        }

        for (JsonNode node : jobs) {

            String title
                    = getText(
                            node,
                            "title"
                    );

            if (!isRelevantTitle(title)) {
                continue;
            }

            String company
                    = board;

            String location
                    = "";

            if (node.has("location")
                    && node.get("location").has("name")) {

                location
                        = node.get("location")
                                .get("name")
                                .asText();
            }

            String jobUrl
                    = getText(
                            node,
                            "absolute_url"
                    );

            String description
                    = cleanHtml(
                            getText(
                                    node,
                                    "content"
                            )
                    );

            String externalId
                    = "greenhouse-"
                    + board
                    + "-"
                    + node.get("id").asText();

            LocalDateTime publishedAt
                    = null;

            if (node.has("updated_at")) {

                try {

                    OffsetDateTime offsetDateTime
                            = OffsetDateTime.parse(
                                    node.get("updated_at")
                                            .asText()
                            );

                    publishedAt
                            = offsetDateTime.toLocalDateTime();

                } catch (Exception ignored) {
                }
            }

            Job job
                    = new Job(
                            externalId,
                            title,
                            company,
                            location,
                            jobUrl,
                            publishedAt,
                            description
                    );

            boolean saved
                    = repository.save(
                            job
                    );

            if (saved) {

                newJobs.add(
                        job
                );

                System.out.println(
                        "[Greenhouse] Saved: "
                        + title
                        + " @ "
                        + company
                );
            }
        }

        return newJobs;
    }

    private boolean isRelevantTitle(
            String title
    ) {

        if (title == null
                || title.isBlank()) {

            return false;
        }

        String lower
                = title.toLowerCase();

        // ==========================
        // Exclude overly senior roles
        // ==========================
        if (lower.contains("senior staff")
                || lower.contains("staff engineer")
                || lower.contains("staff software")
                || lower.contains("staff backend")
                || lower.contains("staff fullstack")
                || lower.contains("staff full stack")
                || lower.contains("principal")
                || lower.contains("lead engineer")
                || lower.contains("engineering manager")
                || lower.contains("manager, engineering")
                || lower.contains("software architect")
                || lower.contains("solutions architect")
                || lower.contains("director")
                || lower.contains("vice president")
                || lower.contains(" vp ")) {

            return false;
        }

        // ==========================
        // Keep relevant SWE roles
        // ==========================
        return lower.contains("software engineer")
                || lower.contains("backend engineer")
                || lower.contains("backend developer")
                || lower.contains("java developer")
                || lower.contains("java engineer")
                || lower.contains("full stack engineer")
                || lower.contains("fullstack engineer")
                || lower.contains("software developer");
    }

    private String getText(
            JsonNode node,
            String field
    ) {

        JsonNode value
                = node.get(
                        field
                );

        if (value == null
                || value.isNull()) {

            return "";
        }

        return value.asText();
    }

    private String cleanHtml(
            String html
    ) {

        if (html == null) {
            return "";
        }

        return html
                .replaceAll(
                        "<[^>]*>",
                        " "
                )
                .replaceAll(
                        "&nbsp;",
                        " "
                )
                .replaceAll(
                        "&amp;",
                        "&"
                )
                .replaceAll(
                        "&#39;",
                        "'"
                )
                .replaceAll(
                        "&quot;",
                        "\""
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }
}

