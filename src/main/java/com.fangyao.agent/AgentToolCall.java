package com.fangyao.agent;

public class AgentToolCall {

    private Long id;
    private Long messageId;
    private String toolName;
    private String arguments;
    private String result;

    public AgentToolCall() {
    }

    public AgentToolCall(
            Long messageId,
            String toolName,
            String arguments,
            String result) {

        this.messageId = messageId;
        this.toolName = toolName;
        this.arguments = arguments;
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}