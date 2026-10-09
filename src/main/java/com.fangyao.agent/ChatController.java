package com.fangyao.agent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatConversationRepository conversationRepository;

    public ChatController(
            ChatService chatService
    ) {

        this.chatService =
                chatService;

        this.conversationRepository =
                new ChatConversationRepository();
    }

    // =========================
    // Normal non-streaming chat
    // =========================
    @PostMapping
    public ChatResponse chat(
            @RequestBody ChatRequest request
    ) {

        Long conversationId =
                request.getConversationId();

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

        String message =
                request.getMessage();

        if (message == null
                || message.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "message is required."
            );
        }

        ChatResult result =
                chatService.chat(
                        conversationId,
                        message
                );

        return new ChatResponse(
                result.getResponse(),
                result.getToolsUsed()
        );
    }

    // =========================
    // Streaming chat
    // =========================
    @PostMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter chatStream(
            @RequestBody ChatRequest request
    ) {

        Long conversationId =
                request.getConversationId();

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

        String message =
                request.getMessage();

        if (message == null
                || message.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "message is required."
            );
        }

        SseEmitter emitter =
                new SseEmitter(
                        120_000L
                );

        CompletableFuture.runAsync(() -> {

            try {

                emitter.send(
                        SseEmitter.event()
                                .name("status")
                                .data("Thinking...")
                );

                ChatResult result =
                        chatService.chat(
                                conversationId,
                                message,
                                delta -> {

                                    try {

                                        emitter.send(
                                                SseEmitter.event()
                                                        .name("delta")
                                                        .data(delta)
                                        );

                                    } catch (IOException e) {

                                        throw new RuntimeException(
                                                "Failed to stream chat delta.",
                                                e
                                        );
                                    }
                                }
                        );

                emitter.send(
                        SseEmitter.event()
                                .name("message")
                                .data(
                                        new ChatResponse(
                                                result.getResponse(),
                                                result.getToolsUsed()
                                        )
                                )
                );

                emitter.send(
                        SseEmitter.event()
                                .name("done")
                                .data("done")
                );

                emitter.complete();

            } catch (Exception e) {

                try {

                    emitter.send(
                            SseEmitter.event()
                                    .name("error")
                                    .data(
                                            e.getMessage() == null
                                                    ? "Chat failed."
                                                    : e.getMessage()
                                    )
                    );

                } catch (IOException ignored) {
                }

                emitter.completeWithError(
                        e
                );
            }
        });

        return emitter;
    }
}