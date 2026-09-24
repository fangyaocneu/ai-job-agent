package com.fangyao.agent;

import java.time.LocalDateTime;

public class Job {

    private Integer matchScore;
    private String matchReason;
    private String matchGap;
    private Integer id;

    private String externalId;
    private String title;
    private String company;
    private String location;
    private String url;
    private LocalDateTime publishedAt;
    private String description;

    public Integer getMatchScore() {
    return matchScore;
}

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }

    public String getMatchGap() {
        return matchGap;
    }

    public void setMatchGap(String matchGap) {
        this.matchGap = matchGap;
    }

    public Job(
            String externalId,
            String title,
            String company,
            String location,
            String url,
            LocalDateTime publishedAt,
            String description
    ) {
        this.externalId = externalId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.url = url;
        this.publishedAt = publishedAt;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getDescription() {
        return description;
    }
}