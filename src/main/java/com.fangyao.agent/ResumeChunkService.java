package com.fangyao.agent;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeChunkService {

    private static final int MAX_CHUNK_LENGTH =
            1200;

    private final ResumeChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public ResumeChunkService() {

        this.chunkRepository =
                new ResumeChunkRepository();

        this.embeddingService =
                new EmbeddingService();
    }

    public List<String> chunkAndSave(
            long resumeId,
            String resumeText
    ) {

        if (resumeText == null
                || resumeText.isBlank()) {

            throw new IllegalArgumentException(
                    "Resume text cannot be empty."
            );
        }

        List<String> chunks =
                chunkText(
                        resumeText
                );

        chunkRepository.deleteByResumeId(
                resumeId
        );

        for (
                int i = 0;
                i < chunks.size();
                i++
        ) {

            String chunk =
                    chunks.get(i);

            System.out.println(
                    "[RESUME EMBEDDING] Generating embedding for chunk "
                            + i
            );

            List<Float> embedding =
                    embeddingService.createEmbedding(
                            chunk
                    );

            System.out.println(
                    "[RESUME EMBEDDING] Dimension = "
                            + embedding.size()
            );

            chunkRepository.save(
                    resumeId,
                    i,
                    chunk,
                    embedding
            );
        }

        return chunks;
    }

    public List<String> chunkText(
            String text
    ) {

        List<String> chunks =
                new ArrayList<>();

        String normalized =
                text.replace(
                        "\r\n",
                        "\n"
                );

        String[] lines =
                normalized.split("\n");

        StringBuilder currentChunk =
                new StringBuilder();

        for (
                String line :
                lines
        ) {

            String cleaned =
                    line.trim();

            if (cleaned.isBlank()) {
                continue;
            }

            if (
                    currentChunk.length()
                            + cleaned.length()
                            + 1
                            > MAX_CHUNK_LENGTH
            ) {

                if (!currentChunk.isEmpty()) {

                    chunks.add(
                            currentChunk
                                    .toString()
                                    .trim()
                    );

                    currentChunk =
                            new StringBuilder();
                }
            }

            currentChunk
                    .append(
                            cleaned
                    )
                    .append(
                            "\n"
                    );
        }

        if (!currentChunk.isEmpty()) {

            chunks.add(
                    currentChunk
                            .toString()
                            .trim()
            );
        }

        return chunks;
    }
}