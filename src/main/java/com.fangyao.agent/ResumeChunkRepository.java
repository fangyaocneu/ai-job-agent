package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class ResumeChunkRepository {

    public void deleteByResumeId(
            long resumeId
    ) {

        String sql = """
                DELETE FROM resume_chunks
                WHERE resume_id = ?
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    resumeId
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to delete resume chunks.",
                    e
            );
        }
    }

    public void save(
            long resumeId,
            int chunkIndex,
            String content,
            List<Float> embedding
    ) {

        String sql = """
                INSERT INTO resume_chunks (
                    resume_id,
                    chunk_index,
                    content,
                    embedding
                )
                VALUES (?, ?, ?, ?::vector)
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    resumeId
            );

            statement.setInt(
                    2,
                    chunkIndex
            );

            statement.setString(
                    3,
                    content
            );

            statement.setString(
                    4,
                    toPgVector(
                            embedding
                    )
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to save resume chunk.",
                    e
            );
        }
    }

    public List<String> getByResumeId(
            long resumeId
    ) {

        String sql = """
                SELECT content
                FROM resume_chunks
                WHERE resume_id = ?
                ORDER BY chunk_index ASC
                """;

        List<String> chunks
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    resumeId
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {

                    chunks.add(
                            resultSet.getString(
                                    "content"
                            )
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load resume chunks.",
                    e
            );
        }

        return chunks;
    }

    private String toPgVector(
            List<Float> embedding
    ) {

        if (embedding == null
                || embedding.isEmpty()) {

            throw new IllegalArgumentException(
                    "Embedding cannot be empty."
            );
        }

        StringBuilder builder
                = new StringBuilder();

        builder.append("[");

        for (int i = 0;
                i < embedding.size();
                i++) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    embedding.get(i)
            );
        }

        builder.append("]");

        return builder.toString();
    }

    public List<ResumeChunkMatch> searchSimilar(
            List<Float> queryEmbedding,
            int limit
    ) {

        String sql = """
            WITH query_embedding AS (
                SELECT ?::vector AS embedding
            )
            SELECT
                rc.resume_id,
                rc.chunk_index,
                rc.content,
                1 - (
                    rc.embedding <=> q.embedding
                ) AS similarity
            FROM resume_chunks rc
            CROSS JOIN query_embedding q
            WHERE rc.embedding IS NOT NULL
            ORDER BY
                rc.embedding <=> q.embedding
            LIMIT ?
            """;

        List<ResumeChunkMatch> results
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    toPgVector(
                            queryEmbedding
                    )
            );

            statement.setInt(
                    2,
                    limit
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {

                    results.add(
                            new ResumeChunkMatch(
                                    resultSet.getLong(
                                            "resume_id"
                                    ),
                                    resultSet.getInt(
                                            "chunk_index"
                                    ),
                                    resultSet.getString(
                                            "content"
                                    ),
                                    resultSet.getDouble(
                                            "similarity"
                                    )
                            )
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to search resume chunks.",
                    e
            );
        }

        return results;
    }
}
