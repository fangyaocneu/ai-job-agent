package com.fangyao.agent;

public class JobEmailWorkflowTest {

    public static void main(String[] args) {

        String email =
                System.getenv("EMAIL_ADDRESS");

        String appPassword =
                System.getenv("EMAIL_APP_PASSWORD");

        SentJobRepository sentJobRepository =
                new SentJobRepository();

        EmailService emailService =
                new EmailService(
                        email,
                        appPassword
                );

        JobEmailWorkflow workflow =
                new JobEmailWorkflow(
                        sentJobRepository,
                        emailService
                );

        workflow.run(email);
    }
}