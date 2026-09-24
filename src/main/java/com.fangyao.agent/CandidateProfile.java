package com.fangyao.agent;

public class CandidateProfile {

    private String skills;
    private String targetRoles;
    private String experience;

    public CandidateProfile() {
        this.skills = """
                Java
                Spring Boot
                Python
                JavaScript
                SQL
                AWS
                """;

        this.targetRoles = """
                Software Engineer
                Backend Engineer
                Java Developer
                """;

        this.experience = """
                Software testing and automation
                Backend API development
                Full-stack academic projects
                Cloud and database experience
                """;
    }

    public String getSkills() {
        return skills;
    }

    public String getTargetRoles() {
        return targetRoles;
    }

    public String getExperience() {
        return experience;
    }

    public String getProfileSummary() {
        return """
                Skills:
                %s

                Target Roles:
                %s

                Experience:
                %s
                """.formatted(skills, targetRoles, experience);
    }
}