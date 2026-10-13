package com.fangyao.agent.evaluation;

import com.fangyao.agent.AgentIntent;

public class EvaluationCase {

    private String input;
    private AgentIntent expectedIntent;

    public EvaluationCase() {
    }

    public EvaluationCase(
            String input,
            AgentIntent expectedIntent
    ) {
        this.input = input;
        this.expectedIntent = expectedIntent;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public AgentIntent getExpectedIntent() {
        return expectedIntent;
    }

    public void setExpectedIntent(
            AgentIntent expectedIntent
    ) {
        this.expectedIntent = expectedIntent;
    }
}