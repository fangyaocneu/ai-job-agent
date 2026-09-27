package com.fangyao.agent;

import java.util.ArrayList;
import java.util.List;

public class ApplicationStrategy {

    private String recommendation;
    private String priority;

    private List<String> resumeFocus;
    private List<String> concerns;

    private String applicationAdvice;

    public ApplicationStrategy() {
        this.resumeFocus = new ArrayList<>();
        this.concerns = new ArrayList<>();
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(
            String recommendation
    ) {
        this.recommendation = recommendation;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(
            String priority
    ) {
        this.priority = priority;
    }

    public List<String> getResumeFocus() {
        return resumeFocus;
    }

    public void setResumeFocus(
            List<String> resumeFocus
    ) {
        this.resumeFocus = resumeFocus;
    }

    public List<String> getConcerns() {
        return concerns;
    }

    public void setConcerns(
            List<String> concerns
    ) {
        this.concerns = concerns;
    }

    public String getApplicationAdvice() {
        return applicationAdvice;
    }

    public void setApplicationAdvice(
            String applicationAdvice
    ) {
        this.applicationAdvice = applicationAdvice;
    }
}