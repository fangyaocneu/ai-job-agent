package com.fangyao.agent;

public class EmailTest {

    public static void main(String[] args) {

        String sender =
                System.getenv("EMAIL_ADDRESS");

        String appPassword =
                System.getenv("EMAIL_APP_PASSWORD");

        EmailService emailService =
                new EmailService(
                        sender,
                        appPassword
                );

        emailService.sendEmail(
                sender,
                "AI Agent Test Email",
                "Your AI Job Agent email system is working."
        );
    }
}