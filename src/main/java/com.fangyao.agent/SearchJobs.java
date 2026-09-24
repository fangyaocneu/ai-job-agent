package com.fangyao.agent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchJobs {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JobRepository repository;

    public SearchJobs() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.repository = new JobRepository();
    }

    public List<Job> search(String keyword) {

        List<Job> newJobs = new ArrayList<>();

        try {
            String encodedKeyword =
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            String url =
                    "https://remotive.com/api/remote-jobs?search="
                            + encodedKeyword;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                System.out.println(
                        "[SearchJobs] API request failed. Status: "
                                + response.statusCode()
                );

                return newJobs;
            }

            JsonNode root =
                    objectMapper.readTree(response.body());

            JsonNode jobsNode = root.get("jobs");

            if (jobsNode == null || !jobsNode.isArray()) {
                System.out.println(
                        "[SearchJobs] No jobs array found."
                );

                return newJobs;
            }

            for (JsonNode node : jobsNode) {

                String externalId =
                        node.get("id").asText();

                String title =
                        node.get("title").asText();

                String company =
                        node.get("company_name").asText();

                String location =
                        node.get("candidate_required_location").asText();

                String jobUrl =
                        node.get("url").asText();

                String description =
                        node.hasNonNull("description")
                                ? node.get("description").asText()
                                : "";

                /*
                 * Remotive description contains HTML.
                 * For AI matching we don't really need all the HTML tags,
                 * so clean them before storing.
                 */
                description = cleanDescription(description);

                LocalDateTime publishedAt = null;

                if (node.hasNonNull("publication_date")) {

                    String publicationDate =
                            node.get("publication_date").asText();

                    try {
                        publishedAt =
                                LocalDateTime.parse(publicationDate);

                    } catch (Exception e) {
                        System.out.println(
                                "[SearchJobs] Could not parse date: "
                                        + publicationDate
                        );
                    }
                }

                Job job = new Job(
                        externalId,
                        title,
                        company,
                        location,
                        jobUrl,
                        publishedAt,
                        description
                );

                boolean inserted =
                        repository.save(job);

                if (inserted) {
                    newJobs.add(job);

                    System.out.println(
                            "[SearchJobs] New job found: "
                                    + title
                                    + " @ "
                                    + company
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "[SearchJobs] Error while searching jobs: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return newJobs;
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