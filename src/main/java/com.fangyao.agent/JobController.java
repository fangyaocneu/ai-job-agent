package com.fangyao.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import java.util.List;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/jobs")

public class JobController {

    private final JobRepository jobRepository;

    public JobController() {
        this.jobRepository = new JobRepository();
    }

    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.getAllJobs();
    }

    @GetMapping("/high-match")
    public List<Job> getHighMatchJobs() {
        return jobRepository.getHighMatchJobs();
    }

    @GetMapping("/{id}")
    public Job getJobById(@PathVariable int id) {
        return jobRepository.getJobById(id);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return jobRepository.getStats();
    }

    @PatchMapping("/{id}/status")
    public boolean updateJobStatus(
            @PathVariable int id,
            @RequestBody Map<String, Object> body) {

        String status = (String) body.get("status");
        boolean value = (Boolean) body.get("value");

        return jobRepository.updateJobStatus(id, status, value);
    }
}
