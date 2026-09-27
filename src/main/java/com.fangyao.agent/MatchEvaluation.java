package com.fangyao.agent;

import java.util.ArrayList;
import java.util.List;

public class MatchEvaluation {

    private int overallScore;
    private int skillScore;
    private int experienceScore;
    private int roleFitScore;

    private String reason;
    private String gap;

    private List<String> strengths;
    private List<String> missingSkills;

    public MatchEvaluation() {

        this.strengths =
                new ArrayList<>();

        this.missingSkills =
                new ArrayList<>();
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(
            int overallScore
    ) {
        this.overallScore = overallScore;
    }

    public int getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(
            int skillScore
    ) {
        this.skillScore = skillScore;
    }

    public int getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(
            int experienceScore
    ) {
        this.experienceScore = experienceScore;
    }

    public int getRoleFitScore() {
        return roleFitScore;
    }

    public void setRoleFitScore(
            int roleFitScore
    ) {
        this.roleFitScore = roleFitScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason
    ) {
        this.reason = reason;
    }

    public String getGap() {
        return gap;
    }

    public void setGap(
            String gap
    ) {
        this.gap = gap;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(
            List<String> strengths
    ) {
        this.strengths = strengths;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(
            List<String> missingSkills
    ) {
        this.missingSkills = missingSkills;
    }

    public JobMatchResult toJobMatchResult() {

        return new JobMatchResult(
                overallScore,
                reason,
                gap
        );
    }
}