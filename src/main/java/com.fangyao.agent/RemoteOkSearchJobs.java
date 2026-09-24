package com.fangyao.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteOkSearchJobs {

    private static final String API_URL =
            "https://remoteok.com/api?tag=dev";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JobRepository repository;

    public RemoteOkSearchJobs() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.repository = new JobRepository();
    }

    public List<Job> search() {

        List<Job> newJobs = new ArrayList<>();

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .header(
                                    "User-Agent",
                                    "AI-Job-Agent/1.0"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                System.err.println(
                        "[RemoteOK] Request failed. Status: "
                                + response.statusCode()
                );

                return newJobs;
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            if (!root.isArray()) {
                System.err.println(
                        "[RemoteOK] Unexpected API response."
                );
                return newJobs;
            }

            for (JsonNode node : root) {

                // Remote OK includes a metadata object as
                // the first element. Skip anything without an id.
                if (!node.has("id")) {
                    continue;
                }

                String id =
                        node.path("id").asText("");

                String title =
                        node.path("position").asText("");

                String company =
                        node.path("company").asText("");

                String location =
                        node.path("location").asText("Remote");

                String url =
                        node.path("url").asText("");

                String description =
                        cleanDescription(
                                node.path("description")
                                        .asText("")
                        );

                LocalDateTime publishedAt =
                        parsePublishedAt(node);

                if (id.isBlank() || title.isBlank()) {
                    continue;
                }

                Job job =
                        new Job(
                                "remoteok-" + id,
                                title,
                                company,
                                location,
                                url,
                                publishedAt,
                                description
                        );

                boolean inserted =
                        repository.save(job);

                if (inserted) {
                    newJobs.add(job);
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "[RemoteOK] Failed to search jobs."
            );

            e.printStackTrace();
        }

        return newJobs;
    }

    private LocalDateTime parsePublishedAt(
            JsonNode node
    ) {

        try {

            long epoch =
                    node.path("epoch").asLong(0);

            if (epoch == 0) {
                return null;
            }

            return LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(epoch),
                    ZoneId.systemDefault()
            );

        } catch (Exception e) {

            return null;
        }
    }

    private String cleanDescription(
            String description
    ) {

        if (description == null) {
            return "";
        }

        return description
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }
}