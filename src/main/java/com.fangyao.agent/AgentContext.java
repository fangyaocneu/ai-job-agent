package com.fangyao.agent;

public class AgentContext {

    private Job job;

    private boolean relevant;

    private JobAnalysis jobAnalysis;

    private JobMatchResult matchResult;

    private MatchEvaluation matchEvaluation;

    private ApplicationStrategy strategy;

    public AgentContext(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public boolean isRelevant() {
        return relevant;
    }

    public void setRelevant(
            boolean relevant
    ) {
        this.relevant = relevant;
    }

    public JobAnalysis getJobAnalysis() {
        return jobAnalysis;
    }

    public void setJobAnalysis(
            JobAnalysis jobAnalysis
    ) {
        this.jobAnalysis = jobAnalysis;
    }

    public JobMatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(
            JobMatchResult matchResult
    ) {
        this.matchResult = matchResult;
    }

    public MatchEvaluation getMatchEvaluation() {
        return matchEvaluation;
    }

    public void setMatchEvaluation(
            MatchEvaluation matchEvaluation
    ) {
        this.matchEvaluation = matchEvaluation;
    }

    public ApplicationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(
            ApplicationStrategy strategy
    ) {
        this.strategy = strategy;
    }
}