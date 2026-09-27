package com.fangyao.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class LeverJobSource implements JobSource {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final JobRepository repository;

    public LeverJobSource() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.repository = new JobRepository();
    }

    @Override
    public String getName() {
        return "Lever";
    }

    @Override
    public List<Job> fetchJobs() {

        List<Job> newJobs = new ArrayList<>();

        // Lever company site slugs
        List<String> companySites = List.of(
               "palantir"
        );

        for (String site : companySites) {

            try {

                List<Job> jobs =
                        fetchCompanyJobs(site);

                newJobs.addAll(jobs);

            } catch (Exception e) {

                System.err.println(
                        "[Lever] Failed site: "
                                + site
                );

                e.printStackTrace();
            }
        }

        return newJobs;
    }

    private List<Job> fetchCompanyJobs(
            String site
    ) throws Exception {

        List<Job> newJobs = new ArrayList<>();

        String url =
                "https://api.lever.co/v0/postings/"
                        + site
                        + "?mode=json";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .header(
                                "User-Agent",
                                "Mozilla/5.0"
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
                    "[Lever] HTTP "
                            + response.statusCode()
                            + " for site "
                            + site
            );

            return newJobs;
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        if (!root.isArray()) {
            return newJobs;
        }

        for (JsonNode node : root) {

            String title =
                    getText(
                            node,
                            "text"
                    );

            if (!isRelevantTitle(title)) {
                continue;
            }

            String location =
                    extractLocation(node);

            String country =
                    getText(
                            node,
                            "country"
                    );

            /*
             * If Lever gives us a country code, use it
             * to avoid saving obviously non-North-American jobs.
             *
             * US / CA / MX are allowed.
             * Unknown country is allowed and JobPreFilter
             * will make the final decision later.
             */
            if (!isAllowedCountry(country)) {
                continue;
            }

            String description =
                    getText(
                            node,
                            "descriptionPlain"
                    );

            if (description.isBlank()) {

                description =
                        cleanHtml(
                                getText(
                                        node,
                                        "description"
                                )
                        );
            }

            String jobUrl =
                    getText(
                            node,
                            "hostedUrl"
                    );

            String id =
                    getText(
                            node,
                            "id"
                    );

            if (id.isBlank()) {
                continue;
            }

            String externalId =
                    "lever-"
                            + site
                            + "-"
                            + id;

            LocalDateTime publishedAt =
                    extractPublishedAt(node);

            Job job =
                    new Job(
                            externalId,
                            title,
                            site,
                            location,
                            jobUrl,
                            publishedAt,
                            description
                    );

            boolean saved =
                    repository.save(job);

            if (saved) {

                newJobs.add(job);

                System.out.println(
                        "[Lever] Saved: "
                                + title
                                + " @ "
                                + site
                                + " | "
                                + location
                );
            }
        }

        return newJobs;
    }

    private String extractLocation(
            JsonNode node
    ) {

        JsonNode categories =
                node.get("categories");

        if (categories == null
                || categories.isNull()) {

            return "";
        }

        JsonNode allLocations =
                categories.get(
                        "allLocations"
                );

        if (allLocations != null
                && allLocations.isArray()
                && !allLocations.isEmpty()) {

            List<String> locations =
                    new ArrayList<>();

            for (JsonNode location : allLocations) {

                String value =
                        location.asText();

                if (!value.isBlank()) {
                    locations.add(value);
                }
            }

            if (!locations.isEmpty()) {

                return String.join(
                        " | ",
                        locations
                );
            }
        }

        JsonNode location =
                categories.get(
                        "location"
                );

        if (location == null
                || location.isNull()) {

            return "";
        }

        return location.asText();
    }

    private LocalDateTime extractPublishedAt(
            JsonNode node
    ) {

        JsonNode createdAt =
                node.get(
                        "createdAt"
                );

        if (createdAt == null
                || createdAt.isNull()
                || !createdAt.canConvertToLong()) {

            return null;
        }

        try {

            long epochMillis =
                    createdAt.asLong();

            return Instant
                    .ofEpochMilli(
                            epochMillis
                    )
                    .atZone(
                            ZoneId.systemDefault()
                    )
                    .toLocalDateTime();

        } catch (Exception e) {

            return null;
        }
    }

    private boolean isAllowedCountry(
            String country
    ) {

        if (country == null
                || country.isBlank()) {

            return true;
        }

        String normalized =
                country
                        .trim()
                        .toUpperCase();

        return normalized.equals("US")
                || normalized.equals("CA")
                || normalized.equals("MX");
    }

    private boolean isRelevantTitle(
            String title
    ) {

        if (title == null
                || title.isBlank()) {

            return false;
        }

        String lower =
                title.toLowerCase();

        // ==========================
        // Exclude Staff+
        // ==========================
        if (lower.contains("senior staff")
                || lower.contains("staff engineer")
                || lower.contains("staff software")
                || lower.contains("staff backend")
                || lower.contains("principal")
                || lower.contains("lead engineer")
                || lower.contains("engineering manager")
                || lower.contains("software architect")
                || lower.contains("director")
                || lower.contains("vice president")) {

            return false;
        }

        // ==========================
        // Keep SWE-style roles
        // ==========================
        return lower.contains("software engineer")
                || lower.contains("software developer")
                || lower.contains("backend engineer")
                || lower.contains("backend developer")
                || lower.contains("java engineer")
                || lower.contains("java developer")
                || lower.contains("full stack engineer")
                || lower.contains("fullstack engineer");
    }

    private String getText(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);

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