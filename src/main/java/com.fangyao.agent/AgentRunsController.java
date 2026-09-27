package com.fangyao.agent;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentRunsController {

    private final AgentRunRepository repository;

    public AgentRunsController() {
        this.repository =
                new AgentRunRepository();
    }

    @GetMapping("/runs")
    public List<AgentRun> getRecentRuns() {

        return repository
                .findRecentRuns();
    }

    @GetMapping("/runs/job/{jobId}")
    public List<AgentRun> getRunsForJob(
            @PathVariable long jobId
    ) {

        return repository
                .findRunsByJobId(
                        jobId
                );
    }
}