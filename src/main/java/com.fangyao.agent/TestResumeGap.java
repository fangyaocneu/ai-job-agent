package com.fangyao.agent;

public class TestResumeGap {

    public static void main(String[] args) {

        String jobDescription = """
                Backend Software Engineer

                Requirements:
                - Java
                - Spring Boot
                - REST APIs
                - PostgreSQL
                - AWS
                - Docker
                - Microservices
                - Kubernetes
                - CI/CD
                """;

        ResumeGapService service =
                new ResumeGapService();

        ResumeGapService.GapAnalysis result =
                service.analyze(
                        jobDescription
                );

        System.out.println(
                "===== GAP ANALYSIS ====="
        );

        System.out.println(
                result.getAnalysis()
        );
    }
}