package com.fangyao.agent;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private final String senderEmail;
    private final String appPassword;

    public EmailService(
            String senderEmail,
            String appPassword
    ) {
        this.senderEmail = senderEmail;
        this.appPassword = appPassword;
    }

    public boolean sendEmail(
            String recipient,
            String subject,
            String body
    ) {

        try {

            Properties properties =
                    new Properties();

            properties.put(
                    "mail.smtp.auth",
                    "true"
            );

            properties.put(
                    "mail.smtp.starttls.enable",
                    "true"
            );

            properties.put(
                    "mail.smtp.host",
                    "smtp.gmail.com"
            );

            properties.put(
                    "mail.smtp.port",
                    "587"
            );

            Session session =
                    Session.getInstance(
                            properties,
                            new Authenticator() {

                                @Override
                                protected PasswordAuthentication
                                getPasswordAuthentication() {

                                    return new PasswordAuthentication(
                                            senderEmail,
                                            appPassword
                                    );
                                }
                            }
                    );

            Message message =
                    new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(senderEmail)
            );

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );

            message.setSubject(subject);

            message.setText(body);

            Transport.send(message);

            System.out.println(
                    "[Email] Sent successfully."
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "[Email Error] "
                            + e.getMessage()
            );

            return false;
        }
    }
}