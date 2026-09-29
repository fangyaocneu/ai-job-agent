package com.fangyao.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ChatMessageController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageController() {

        this.chatMessageRepository =
                new ChatMessageRepository();
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessage> getMessages(
            @PathVariable long conversationId
    ) {

        return chatMessageRepository
                .getMessagesByConversation(
                        conversationId
                );
    }
}