package com.fangyao.agent;

import java.util.List;

public class ResumeRagService {

    private final EmbeddingService embeddingService;
    private final ResumeChunkRepository chunkRepository;

    public ResumeRagService() {

        this.embeddingService
                = new EmbeddingService();

        this.chunkRepository
                = new ResumeChunkRepository();
    }

    public List<ResumeChunkMatch> retrieveRelevantChunks(
            String query,
            int limit
    ) {

        if (query == null
                || query.isBlank()) {

            throw new IllegalArgumentException(
                    "RAG query cannot be empty."
            );
        }

        int safeLimit
                = Math.max(
                        1,
                        Math.min(
                                limit,
                                5
                        )
                );

        List<Float> queryEmbedding
                = embeddingService.createEmbedding(
                        query
                );

        return chunkRepository.searchSimilar(
                queryEmbedding,
                safeLimit
        );
    }
}

                 
                 
                 
                 
