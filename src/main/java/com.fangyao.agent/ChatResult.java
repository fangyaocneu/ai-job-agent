package com.fangyao.agent;

import java.util.List;

public class ChatResult {

    private final String response;
    private final List<AgentTrace> toolsUsed;

    public ChatResult(
            String response,
            List<AgentTrace> toolsUsed
    ) {
        this.response = response;
        this.toolsUsed = toolsUsed;
    }

    public String getResponse() {
        return response;
    }

    public List<AgentTrace> getToolsUsed() {
        return toolsUsed;
    }
}