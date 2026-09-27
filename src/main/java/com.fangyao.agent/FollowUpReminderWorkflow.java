package com.fangyao.agent;

public class FollowUpReminderWorkflow {

    private final FollowUpReminderService followUpReminderService;
    private final EmailService emailService;

    public FollowUpReminderWorkflow(
            FollowUpReminderService followUpReminderService,
            EmailService emailService
    ) {
        this.followUpReminderService = followUpReminderService;
        this.emailService = emailService;
    }

    public void run(String recipientEmail) {

        String body =
                followUpReminderService.buildReminderEmail();

        if (body == null || body.isBlank()) {

            System.out.println(
                    "[Follow-up Reminder] No follow-ups due."
            );

            return;
        }

        boolean sent =
                emailService.sendEmail(
                        recipientEmail,
                        "AI Job Agent Follow-up Reminder",
                        body
                );

        if (!sent) {

            System.out.println(
                    "[Follow-up Reminder] Email failed."
            );

            return;
        }

        System.out.println(
                "[Follow-up Reminder] Reminder email sent successfully."
        );
    }
}
