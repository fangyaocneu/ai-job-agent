package com.fangyao.agent;

import java.time.LocalDateTime;
import java.time.LocalDate;

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
    private boolean favorite;
    private boolean applied;
    private boolean ignored;
    private String applicationStage;
    private String notes;
    private LocalDate appliedAt;
    private LocalDate followUpDate;

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

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public String getApplicationStage() {
        return applicationStage;
    }

    public void setApplicationStage(String applicationStage) {
        this.applicationStage = applicationStage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDate appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }
}
