package com.fangyao.agent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonClassDescription("Gets the current weather for a city.")
public class GetWeather implements Tool {

    @JsonPropertyDescription("The city to get weather for.")
    public String city;

    @Override
    public String getName() {
        return "GetWeather";
    }

    @Override
    public String execute() {

        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            String encodedCity =
                    URLEncoder.encode(city, StandardCharsets.UTF_8);

            String geoUrl =
                    "https://geocoding-api.open-meteo.com/v1/search"
                    + "?name=" + encodedCity
                    + "&count=1"
                    + "&language=en"
                    + "&format=json";

            HttpRequest geoRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(geoUrl))
                            .GET()
                            .build();

            HttpResponse<String> geoResponse =
                    httpClient.send(
                            geoRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            JsonNode geoJson =
                    mapper.readTree(geoResponse.body());

            JsonNode results =
                    geoJson.get("results");

            if (results == null || results.isEmpty()) {
                return "Could not find city: " + city;
            }

            JsonNode location = results.get(0);

            double latitude =
                    location.get("latitude").asDouble();

            double longitude =
                    location.get("longitude").asDouble();

            String resolvedCity =
                    location.get("name").asText();

            String weatherUrl =
                    "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&current=temperature_2m,apparent_temperature,weather_code"
                    + "&temperature_unit=fahrenheit";

            HttpRequest weatherRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(weatherUrl))
                            .GET()
                            .build();

            HttpResponse<String> weatherResponse =
                    httpClient.send(
                            weatherRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            JsonNode weatherJson =
                    mapper.readTree(weatherResponse.body());

            JsonNode current =
                    weatherJson.get("current");

            if (current == null) {
                return "Weather data unavailable for " + city;
            }

            double temperature =
                    current.get("temperature_2m").asDouble();

            double feelsLike =
                    current.get("apparent_temperature").asDouble();

            return "Current weather in "
                    + resolvedCity
                    + ": "
                    + temperature
                    + " F, feels like "
                    + feelsLike
                    + " F.";

        } catch (Exception e) {
            return "Failed to get weather for "
                    + city
                    + ": "
                    + e.getMessage();
        }
    }
}