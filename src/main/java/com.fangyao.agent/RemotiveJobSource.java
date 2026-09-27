package com.fangyao.agent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RemotiveJobSource implements JobSource {

    private static final String[] KEYWORDS = {
        "Java",
        "backend",
        "software engineer"
    };

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JobRepository repository;

    public RemotiveJobSource() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.repository = new JobRepository();
    }

    @Override
    public String getName() {
        return "Remotive";
    }

    @Override
    public List<Job> fetchJobs() {

        List<Job> allNewJobs = new ArrayList<>();

        for (String keyword : KEYWORDS) {

            List<Job> jobsForKeyword
                    = searchByKeyword(keyword);

            if (jobsForKeyword.isEmpty()) {

                System.out.println(
                        "[Remotive] No new jobs found for keyword: "
                        + keyword
                );

            } else {

                System.out.println(
                        "[Remotive] Found "
                        + jobsForKeyword.size()
                        + " new jobs for keyword: "
                        + keyword
                );
            }

            allNewJobs.addAll(jobsForKeyword);
        }

        return allNewJobs;
    }

    private List<Job> searchByKeyword(String keyword) {

        List<Job> newJobs = new ArrayList<>();

        try {
            String encodedKeyword
                    = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            String url
                    = "https://remotive.com/api/remote-jobs?search="
                    + encodedKeyword;

            HttpRequest request
                    = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

            HttpResponse<String> response
                    = httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                System.out.println(
                        "[Remotive] API request failed. Status: "
                        + response.statusCode()
                );

                return newJobs;
            }

            JsonNode root
                    = objectMapper.readTree(response.body());

            JsonNode jobsNode
                    = root.get("jobs");

            if (jobsNode == null || !jobsNode.isArray()) {
                System.out.println(
                        "[Remotive] No jobs array found."
                );

                return newJobs;
            }

            for (JsonNode node : jobsNode) {

                String externalId
                        = node.path("id").asText();

                String title
                        = node.path("title").asText();

                String company
                        = node.path("company_name").asText();

                String location
                        = node.path("candidate_required_location").asText();

                String jobUrl
                        = node.path("url").asText();

                String description
                        = node.hasNonNull("description")
                        ? node.get("description").asText()
                        : "";

                description
                        = cleanDescription(description);

                LocalDateTime publishedAt
                        = parsePublishedAt(node);

                if (externalId.isBlank()
                        || title.isBlank()
                        || jobUrl.isBlank()) {
                    continue;
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

                boolean inserted
                        = repository.save(job);

                if (inserted) {
                    newJobs.add(job);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "[Remotive] Error while searching jobs: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }

        return newJobs;
    }

    private LocalDateTime parsePublishedAt(JsonNode node) {

        if (!node.hasNonNull("publication_date")) {
            return null;
        }

        String publicationDate
                = node.get("publication_date").asText();

        try {
            return OffsetDateTime
                    .parse(publicationDate)
                    .toLocalDateTime();

        } catch (Exception e) {

            try {
                return LocalDateTime.parse(publicationDate);

            } catch (Exception ex) {
                System.out.println(
                        "[Remotive] Could not parse date: "
                        + publicationDate
                );
                return null;
            }
        }
    }

    private String cleanDescription(String description) {

        if (description == null) {
            return "";
        }

        return description
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
