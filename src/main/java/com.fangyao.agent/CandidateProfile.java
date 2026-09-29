package com.fangyao.agent;

import java.util.ArrayList;
import java.util.List;

public class CandidateProfile {

    private Long id;
    private String name;

    private List<String> languages;
    private List<String> backendTechnologies;
    private List<String> cloudTechnologies;
    private List<String> databases;
    private List<String> frontendTechnologies;
    private List<String> tools;

    private List<String> targetRoles;

    private List<String> experienceHighlights;
    private List<String> projectHighlights;

    private List<String> preferredLocations;
    private List<String> preferredWorkModes;

    public CandidateProfile() {

        this.languages =
                new ArrayList<>();

        this.backendTechnologies =
                new ArrayList<>();

        this.cloudTechnologies =
                new ArrayList<>();

        this.databases =
                new ArrayList<>();

        this.frontendTechnologies =
                new ArrayList<>();

        this.tools =
                new ArrayList<>();

        this.targetRoles =
                new ArrayList<>();

        this.experienceHighlights =
                new ArrayList<>();

        this.projectHighlights =
                new ArrayList<>();

        this.preferredLocations =
                new ArrayList<>();

        this.preferredWorkModes =
                new ArrayList<>();
    }

    // ==========================
    // ID
    // ==========================

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    // ==========================
    // Name
    // ==========================

    public String getName() {
        return name;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }

    // ==========================
    // Languages
    // ==========================

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(
            List<String> languages
    ) {

        this.languages =
                safeList(
                        languages
                );
    }

    // ==========================
    // Backend
    // ==========================

    public List<String> getBackendTechnologies() {
        return backendTechnologies;
    }

    public void setBackendTechnologies(
            List<String> backendTechnologies
    ) {

        this.backendTechnologies =
                safeList(
                        backendTechnologies
                );
    }

    // ==========================
    // Cloud
    // ==========================

    public List<String> getCloudTechnologies() {
        return cloudTechnologies;
    }

    public void setCloudTechnologies(
            List<String> cloudTechnologies
    ) {

        this.cloudTechnologies =
                safeList(
                        cloudTechnologies
                );
    }

    // ==========================
    // Databases
    // ==========================

    public List<String> getDatabases() {
        return databases;
    }

    public void setDatabases(
            List<String> databases
    ) {

        this.databases =
                safeList(
                        databases
                );
    }

    // ==========================
    // Frontend
    // ==========================

    public List<String> getFrontendTechnologies() {
        return frontendTechnologies;
    }

    public void setFrontendTechnologies(
            List<String> frontendTechnologies
    ) {

        this.frontendTechnologies =
                safeList(
                        frontendTechnologies
                );
    }

    // ==========================
    // Tools
    // ==========================

    public List<String> getTools() {
        return tools;
    }

    public void setTools(
            List<String> tools
    ) {

        this.tools =
                safeList(
                        tools
                );
    }

    // ==========================
    // Target Roles
    // ==========================

    public List<String> getTargetRoles() {
        return targetRoles;
    }

    public void setTargetRoles(
            List<String> targetRoles
    ) {

        this.targetRoles =
                safeList(
                        targetRoles
                );
    }

    // ==========================
    // Experience
    // ==========================

    public List<String> getExperienceHighlights() {
        return experienceHighlights;
    }

    public void setExperienceHighlights(
            List<String> experienceHighlights
    ) {

        this.experienceHighlights =
                safeList(
                        experienceHighlights
                );
    }

    // ==========================
    // Projects
    // ==========================

    public List<String> getProjectHighlights() {
        return projectHighlights;
    }

    public void setProjectHighlights(
            List<String> projectHighlights
    ) {

        this.projectHighlights =
                safeList(
                        projectHighlights
                );
    }

    // ==========================
    // Preferred Locations
    // ==========================

    public List<String> getPreferredLocations() {
        return preferredLocations;
    }

    public void setPreferredLocations(
            List<String> preferredLocations
    ) {

        this.preferredLocations =
                safeList(
                        preferredLocations
                );
    }

    // ==========================
    // Preferred Work Modes
    // ==========================

    public List<String> getPreferredWorkModes() {
        return preferredWorkModes;
    }

    public void setPreferredWorkModes(
            List<String> preferredWorkModes
    ) {

        this.preferredWorkModes =
                safeList(
                        preferredWorkModes
                );
    }

    // ==========================
    // Profile Summary
    // ==========================

    public String getProfileSummary() {

        return """
                Name:
                %s

                Languages:
                %s

                Backend:
                %s

                Cloud:
                %s

                Databases:
                %s

                Frontend:
                %s

                Tools:
                %s

                Target Roles:
                %s

                Experience:
                %s

                Projects:
                %s

                Preferred Locations:
                %s

                Preferred Work Modes:
                %s
                """
                .formatted(
                        name,
                        languages,
                        backendTechnologies,
                        cloudTechnologies,
                        databases,
                        frontendTechnologies,
                        tools,
                        targetRoles,
                        experienceHighlights,
                        projectHighlights,
                        preferredLocations,
                        preferredWorkModes
                );
    }

    private List<String> safeList(
            List<String> values
    ) {

        if (values == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                values
        );
    }
}