package com.fangyao.agent;

public class ChatRequest {

    private Long conversationId;
    private String message;

    public ChatRequest() {
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(
            Long conversationId
    ) {
        this.conversationId =
                conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message =
                message;
    }
}