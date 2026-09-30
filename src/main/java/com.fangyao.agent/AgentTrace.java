package com.fangyao.agent;

public class AgentTrace {

    private final String toolName;
    private final String arguments;
    private final String result;

    public AgentTrace(
            String toolName,
            String arguments,
            String result
    ) {
        this.toolName = toolName;
        this.arguments = arguments;
        this.result = result;
    }

    public String getToolName() {
        return toolName;
    }

    public String getArguments() {
        return arguments;
    }

    public String getResult() {
        return result;
    }
}