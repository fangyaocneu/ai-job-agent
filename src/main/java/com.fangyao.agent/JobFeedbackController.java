package com.fangyao.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobFeedbackController {

    private final JobFeedbackRepository repository;

    public JobFeedbackController() {
        this.repository =
                new JobFeedbackRepository();
    }

    @GetMapping("/{jobId}/feedback")
    public ResponseEntity<JobFeedback> getFeedback(
            @PathVariable long jobId
    ) {

        JobFeedback feedback =
                repository.findByJobId(
                        jobId
                );

        if (feedback == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                feedback
        );
    }

    @PostMapping("/{jobId}/feedback")
    public ResponseEntity<JobFeedback> saveFeedback(
            @PathVariable long jobId,
            @RequestBody JobFeedback feedback
    ) {

        if (
                feedback.getFeedbackLabel() == null
                || feedback.getFeedbackLabel().isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        String label =
                feedback
                        .getFeedbackLabel()
                        .trim()
                        .toUpperCase();

        if (
                !label.equals("LIKE")
                && !label.equals("DISLIKE")
        ) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        feedback.setFeedbackLabel(
                label
        );

        JobFeedback saved =
                repository.saveOrUpdate(
                        jobId,
                        feedback
                );

        if (saved == null) {

            return ResponseEntity
                    .internalServerError()
                    .build();
        }

        return ResponseEntity.ok(
                saved
        );
    }
}