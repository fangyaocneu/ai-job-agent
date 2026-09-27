package com.fangyao.agent;

import java.util.ArrayList;
import java.util.List;

public class JobAnalysis {

    private String roleType;

    private String seniority;

    private List<String> primarySkills;

    private List<String> secondarySkills;

    private Integer requiredYearsExperience;

    private String summary;

    public JobAnalysis() {

        this.primarySkills =
                new ArrayList<>();

        this.secondarySkills =
                new ArrayList<>();
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(
            String roleType
    ) {
        this.roleType = roleType;
    }

    public String getSeniority() {
        return seniority;
    }

    public void setSeniority(
            String seniority
    ) {
        this.seniority = seniority;
    }

    public List<String> getPrimarySkills() {
        return primarySkills;
    }

    public void setPrimarySkills(
            List<String> primarySkills
    ) {
        this.primarySkills = primarySkills;
    }

    public List<String> getSecondarySkills() {
        return secondarySkills;
    }

    public void setSecondarySkills(
            List<String> secondarySkills
    ) {
        this.secondarySkills = secondarySkills;
    }

    public Integer getRequiredYearsExperience() {
        return requiredYearsExperience;
    }

    public void setRequiredYearsExperience(
            Integer requiredYearsExperience
    ) {
        this.requiredYearsExperience =
                requiredYearsExperience;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(
            String summary
    ) {
        this.summary = summary;
    }
}
