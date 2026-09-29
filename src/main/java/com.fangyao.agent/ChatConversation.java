package com.fangyao.agent;

import java.time.LocalDateTime;

public class ChatConversation {

    private long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChatConversation() {
    }

    public long getId() {
        return id;
    }

    public void setId(
            long id
    ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }
}