package com.fangyao.agent;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatConversationRepository conversationRepository;

    public ChatController(
            ChatService chatService
    ) {

        this.chatService
                = chatService;

        this.conversationRepository
                = new ChatConversationRepository();
    }

    @PostMapping
    public ChatResponse chat(
            @RequestBody ChatRequest request
    ) {

        Long conversationId
                = request.getConversationId();

        if (conversationId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "conversationId is required."
            );
        }

        if (!conversationRepository.exists(
                conversationId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Conversation not found."
            );
        }

        String message
                = request.getMessage();

        if (message == null
                || message.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "message is required."
            );
        }

        ChatResult result
                = chatService.chat(
                        conversationId,
                        message
                );

        return new ChatResponse(
                result.getResponse(),
                result.getToolsUsed()
        );
    }
}
