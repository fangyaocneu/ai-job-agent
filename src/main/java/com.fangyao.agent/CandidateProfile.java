package com.fangyao.agent;

import java.util.List;

public class CandidateProfile {

    private final List<String> languages;
    private final List<String> backendTechnologies;
    private final List<String> cloudTechnologies;
    private final List<String> databases;
    private final List<String> frontendTechnologies;
    private final List<String> tools;

    private final List<String> targetRoles;

    private final List<String> experienceHighlights;
    private final List<String> projectHighlights;

    public CandidateProfile() {

        this.languages =
                List.of(
                        "Java",
                        "Python",
                        "JavaScript",
                        "C++",
                        "SQL"
                );

        this.backendTechnologies =
                List.of(
                        "Spring Boot",
                        "REST APIs",
                        "Backend API Development"
                );

        this.cloudTechnologies =
                List.of(
                        "AWS",
                        "Docker",
                        "ECS Fargate",
                        "RDS",
                        "S3",
                        "CloudFront"
                );

        this.databases =
                List.of(
                        "PostgreSQL",
                        "MySQL",
                        "Redshift"
                );

        this.frontendTechnologies =
                List.of(
                        "React"
                );

        this.tools =
                List.of(
                        "Git",
                        "Maven",
                        "n8n"
                );

        this.targetRoles =
                List.of(
                        "Software Engineer",
                        "Backend Engineer",
                        "Java Developer"
                );

        this.experienceHighlights =
                List.of(
                        "Software testing and automation",
                        "Backend API development",
                        "Full-stack application development",
                        "Cloud deployment and infrastructure",
                        "Database design and integration"
                );

        this.projectHighlights =
                List.of(
                        "AI job search agent with Java, Spring Boot, React, PostgreSQL and AWS",
                        "Medical platform using React and Spring Boot",
                        "AI branding automation workflow using OpenAI and n8n"
                );
    }

    public List<String> getLanguages() {
        return languages;
    }

    public List<String> getBackendTechnologies() {
        return backendTechnologies;
    }

    public List<String> getCloudTechnologies() {
        return cloudTechnologies;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public List<String> getFrontendTechnologies() {
        return frontendTechnologies;
    }

    public List<String> getTools() {
        return tools;
    }

    public List<String> getTargetRoles() {
        return targetRoles;
    }

    public List<String> getExperienceHighlights() {
        return experienceHighlights;
    }

    public List<String> getProjectHighlights() {
        return projectHighlights;
    }

    public String getProfileSummary() {

        return """
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
                """
                .formatted(
                        languages,
                        backendTechnologies,
                        cloudTechnologies,
                        databases,
                        frontendTechnologies,
                        tools,
                        targetRoles,
                        experienceHighlights,
                        projectHighlights
                );
    }
}