package com.fangyao.agent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(
            ChatService chatService
    ) {

        this.chatService =
                chatService;
    }

    @PostMapping
    public ChatResponse chat(
            @RequestBody ChatRequest request
    ) {

        Long conversationId =
                request.getConversationId();

        if (
                conversationId == null
        ) {

            conversationId =
                    1L;
        }

        String response =
                chatService.chat(
                        conversationId,
                        request.getMessage()
                );

        return new ChatResponse(
                response
        );
    }
}