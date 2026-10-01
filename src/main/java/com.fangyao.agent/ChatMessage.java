package com.fangyao.agent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatMessage {

    private long id;
    private long conversationId;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    private List<AgentTrace> toolsUsed = new ArrayList<>();

    public ChatMessage() {
    }

    public ChatMessage(
            long conversationId,
            String role,
            String content
    ) {

        this.conversationId
                = conversationId;

        this.role
                = role;

        this.content
                = content;
    }

    public long getId() {
        return id;
    }

    public void setId(
            long id
    ) {
        this.id
                = id;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(
            long conversationId
    ) {
        this.conversationId
                = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role
    ) {
        this.role
                = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(
            String content
    ) {
        this.content
                = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt
                = createdAt;
    }

    public List<AgentTrace> getToolsUsed() {
        return toolsUsed;
    }

    public void setToolsUsed(
            List<AgentTrace> toolsUsed
    ) {
        this.toolsUsed
                = toolsUsed;
    }
}
