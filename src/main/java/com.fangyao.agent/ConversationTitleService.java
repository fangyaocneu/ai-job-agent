package com.fangyao.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;

import java.util.ArrayList;
import java.util.List;

public class ConversationTitleService {

    private final OpenAIClient client;

    public ConversationTitleService() {
        this.client =
                OpenAIOkHttpClient.fromEnv();
    }

    public String generateTitle(
            String firstUserMessage
    ) {

        if (firstUserMessage == null
                || firstUserMessage.isBlank()) {

            return "New Chat";
        }

        String prompt =
                """
                Generate a short conversation title
                based on the user's first message.

                Rules:
                - 3 to 6 words
                - concise
                - no quotation marks
                - no period at the end
                - describe the main topic
                - do not include explanations

                User message:

                %s
                """
                .formatted(
                        firstUserMessage
                );

        List<ResponseInputItem> inputs =
                new ArrayList<>();

        inputs.add(
                ResponseInputItem.ofMessage(
                        ResponseInputItem.Message
                                .builder()
                                .role(
                                        ResponseInputItem.Message.Role.USER
                                )
                                .addInputTextContent(
                                        prompt
                                )
                                .build()
                )
        );

        ResponseCreateParams params =
                ResponseCreateParams
                        .builder()
                        .model(
                                ChatModel.GPT_5_2
                        )
                        .input(
                                ResponseCreateParams.Input
                                        .ofResponse(
                                                inputs
                                        )
                        )
                        .build();

        Response response =
                client.responses()
                        .create(
                                params
                        );

        String title =
                extractOutputText(
                        response
                );

        title =
                cleanTitle(
                        title
                );

        if (title.isBlank()) {
            return "New Chat";
        }

        return title;
    }

    private String cleanTitle(
            String title
    ) {

        String cleaned =
                title
                        .trim()
                        .replace("\"", "")
                        .replace("'", "");

        if (cleaned.endsWith(".")) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 1
                    );
        }

        if (cleaned.length() > 80) {

            cleaned =
                    cleaned.substring(
                            0,
                            80
                    )
                    .trim();
        }

        return cleaned;
    }

    private String extractOutputText(
            Response response
    ) {

        StringBuilder output =
                new StringBuilder();

        response.output()
                .stream()
                .flatMap(
                        item ->
                                item.message()
                                        .stream()
                )
                .flatMap(
                        message ->
                                message.content()
                                        .stream()
                )
                .flatMap(
                        content ->
                                content.outputText()
                                        .stream()
                )
                .forEach(
                        text ->
                                output.append(
                                        text.text()
                                )
                );

        if (output.isEmpty()) {

            throw new IllegalStateException(
                    "Could not generate conversation title."
            );
        }

        return output
                .toString()
                .trim();
    }
}