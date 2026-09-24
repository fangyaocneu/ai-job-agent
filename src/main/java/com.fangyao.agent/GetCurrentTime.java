package com.fangyao.agent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonClassDescription("Gets the current local time for a city.")
public class GetCurrentTime implements Tool {

    @JsonPropertyDescription("The city to get the local time for.")
    public String city;

    @Override
    public String getName() {
        return "GetCurrentTime";
    }

    @Override
    public String execute() {

        try {

            HttpClient httpClient =
                    HttpClient.newHttpClient();

            ObjectMapper mapper =
                    new ObjectMapper();

            // Convert city name into a URL-safe value
            String encodedCity =
                    URLEncoder.encode(
                            city,
                            StandardCharsets.UTF_8
                    );

            // Open-Meteo geocoding API
            String geoUrl =
                    "https://geocoding-api.open-meteo.com/v1/search"
                    + "?name=" + encodedCity
                    + "&count=1"
                    + "&language=en"
                    + "&format=json";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(geoUrl))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            // Convert JSON response into a Java JSON tree
            JsonNode json =
                    mapper.readTree(
                            response.body()
                    );

            JsonNode results =
                    json.get("results");

            if (results == null || results.isEmpty()) {
                return "Could not find city: " + city;
            }

            JsonNode location =
                    results.get(0);

            String resolvedCity =
                    location.get("name").asText();

            String timezone =
                    location.get("timezone").asText();

            System.out.println(
                    "[Timezone] "
                    + resolvedCity
                    + " -> "
                    + timezone
            );

            // Convert timezone string into Java ZoneId
            ZoneId zoneId =
                    ZoneId.of(timezone);

            // Get current time in that city's timezone
            ZonedDateTime currentTime =
                    ZonedDateTime.now(zoneId);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm:ss z"
                    );

            String formattedTime =
                    currentTime.format(formatter);

            return "The current time in "
                    + resolvedCity
                    + " is "
                    + formattedTime
                    + ".";

        } catch (Exception e) {

            return "Failed to get current time for "
                    + city
                    + ": "
                    + e.getMessage();
        }
    }
}