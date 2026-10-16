
package com.fangyao.agent.skills;

import com.openai.models.responses.ResponseInputItem;

public class ChatRequestInputFactory {

    public record PreparedInput(
            String prompt,
            ResponseInputItem input
    ) {
    }

    public PreparedInput prepare(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt cannot be empty"
            );
        }

        ResponseInputItem input =
                ResponseInputItem.ofMessage(
                        ResponseInputItem.Message
                                .builder()
                                .role(
                                        ResponseInputItem.Message.Role.USER
                                )
                                .addInputTextContent(prompt)
                                .build()
                );

        return new PreparedInput(prompt, input);
    }
}
