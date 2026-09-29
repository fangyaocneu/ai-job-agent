package com.fangyao.agent;

public class JobAgentResult {

    private long jobId;

    private String roleType;
    private String seniority;

    private String primarySkills;
    private String secondarySkills;
    private Integer requiredYearsExperience;
    private String analysisSummary;

    private Integer overallScore;
    private Integer skillScore;
    private Integer experienceScore;
    private Integer roleFitScore;

    private Integer preferenceScore;
    private Integer finalScore;

    private String strengths;
    private String missingSkills;

    private String recommendation;
    private String priority;

    private String resumeFocus;
    private String concerns;
    private String applicationAdvice;

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(String seniority) {
        this.seniority = seniority;
    }

    public String getPrimarySkills() {
        return primarySkills;
    }

    public void setPrimarySkills(String primarySkills) {
        this.primarySkills = primarySkills;
    }

    public String getSecondarySkills() {
        return secondarySkills;
    }

    public void setSecondarySkills(String secondarySkills) {
        this.secondarySkills = secondarySkills;
    }

    public Integer getRequiredYearsExperience() {
        return requiredYearsExperience;
    }

    public void setRequiredYearsExperience(Integer requiredYearsExperience) {
        this.requiredYearsExperience = requiredYearsExperience;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(Integer skillScore) {
        this.skillScore = skillScore;
    }

    public Integer getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(Integer experienceScore) {
        this.experienceScore = experienceScore;
    }

    public Integer getRoleFitScore() {
        return roleFitScore;
    }

    public void setRoleFitScore(Integer roleFitScore) {
        this.roleFitScore = roleFitScore;
    }

    public Integer getPreferenceScore() {
        return preferenceScore;
    }

    public void setPreferenceScore(Integer preferenceScore) {
        this.preferenceScore = preferenceScore;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getResumeFocus() {
        return resumeFocus;
    }

    public void setResumeFocus(String resumeFocus) {
        this.resumeFocus = resumeFocus;
    }

    public String getConcerns() {
        return concerns;
    }

    public void setConcerns(String concerns) {
        this.concerns = concerns;
    }

    public String getApplicationAdvice() {
        return applicationAdvice;
    }

    public void setApplicationAdvice(String applicationAdvice) {
        this.applicationAdvice = applicationAdvice;
    }
}