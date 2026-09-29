package com.fangyao.agent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ChatConversationController {

    private final ChatConversationRepository conversationRepository;

    public ChatConversationController() {

        this.conversationRepository
                = new ChatConversationRepository();
    }

    // =========================
    // Get All Conversations
    // =========================
    @GetMapping
    public List<ChatConversation> getAllConversations() {

        return conversationRepository
                .getAll();
    }

    // =========================
    // Create Conversation
    // =========================
    @PostMapping
    public ChatConversation createConversation(
            @RequestBody Map<String, String> request
    ) {

        String title
                = request.get(
                        "title"
                );

        if (title == null
                || title.isBlank()) {

            title
                    = "New Conversation";
        }

        return conversationRepository
                .create(
                        title
                );
    }

    @DeleteMapping("/{conversationId}")
    public void deleteConversation(
            @PathVariable long conversationId
    ) {

        conversationRepository.delete(
                conversationId
        );
    }
}
