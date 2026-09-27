package com.fangyao.agent;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class AgentInsightsController {

    private final JobAgentResultRepository repository;

    public AgentInsightsController() {
        this.repository =
                new JobAgentResultRepository();
    }

    @GetMapping("/{jobId}/agent-insights")
    public ResponseEntity<JobAgentResult> getAgentInsights(
            @PathVariable long jobId
    ) {

        JobAgentResult result =
                repository.findByJobId(jobId);

        if (result == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(result);
    }
}
