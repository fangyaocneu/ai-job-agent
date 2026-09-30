package com.fangyao.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;

import java.util.List;

public class EmbeddingService {

    private final OpenAIClient client;

    public EmbeddingService() {

        this.client =
                OpenAIOkHttpClient.fromEnv();
    }

    public List<Float> createEmbedding(
            String text
    ) {

        if (text == null
                || text.isBlank()) {

            throw new IllegalArgumentException(
                    "Embedding text cannot be empty."
            );
        }

        EmbeddingCreateParams params =
                EmbeddingCreateParams
                        .builder()
                        .model(
                                EmbeddingModel.TEXT_EMBEDDING_3_SMALL
                        )
                        .input(
                                text
                        )
                        .build();

        CreateEmbeddingResponse response =
                client.embeddings()
                        .create(
                                params
                        );

        if (response.data().isEmpty()) {

            throw new IllegalStateException(
                    "OpenAI returned no embedding."
            );
        }

        return response
                .data()
                .get(0)
                .embedding();
    }
}