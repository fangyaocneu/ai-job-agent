package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatMessageRepository {

    private final AgentToolCallRepository agentToolCallRepository
            = new AgentToolCallRepository();

    // =========================
    // Save Message
    // =========================
    public long save(
            long conversationId,
            String role,
            String content
    ) {

        String sql = """
            INSERT INTO chat_messages (
                conversation_id,
                role,
                content
            )
            VALUES (?, ?, ?)
            """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            statement.setLong(
                    1,
                    conversationId
            );

            statement.setString(
                    2,
                    role
            );

            statement.setString(
                    3,
                    content
            );

            statement.executeUpdate();

            try (
                    ResultSet generatedKeys
                    = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    return generatedKeys.getLong(1);
                }
            }

            throw new RuntimeException(
                    "Chat message saved but no ID was returned."
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to save chat message.",
                    e
            );
        }
    }

    // =========================
    // Load Recent Messages
    // Used by AI memory
    // =========================
    public List<ChatMessage> getRecentMessages(
            long conversationId,
            int limit
    ) {

        String sql = """
                SELECT
                    id,
                    conversation_id,
                    role,
                    content,
                    created_at
                FROM chat_messages
                WHERE conversation_id = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        List<ChatMessage> messages
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    conversationId
            );

            statement.setInt(
                    2,
                    limit
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {

                    messages.add(
                            mapMessage(
                                    resultSet
                            )
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load recent chat messages.",
                    e
            );
        }

        // SQL returns newest -> oldest.
        // LLM should receive oldest -> newest.
        Collections.reverse(
                messages
        );

        return messages;
    }

    // =========================
    // Load Full Conversation
    // Used by frontend
    // =========================
    public List<ChatMessage> getMessagesByConversation(
            long conversationId
    ) {

        String sql = """
                SELECT
                    id,
                    conversation_id,
                    role,
                    content,
                    created_at
                FROM chat_messages
                WHERE conversation_id = ?
                ORDER BY created_at ASC, id ASC
                """;

        List<ChatMessage> messages
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    conversationId
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {

                    ChatMessage message
                            = mapMessage(
                                    resultSet
                            );

                    List<AgentToolCall> toolCalls
                            = agentToolCallRepository.findByMessageId(
                                    message.getId()
                            );

                    List<AgentTrace> traces
                            = new ArrayList<>();

                    for (AgentToolCall toolCall : toolCalls) {

                        traces.add(
                                new AgentTrace(
                                        toolCall.getToolName(),
                                        toolCall.getArguments(),
                                        toolCall.getResult()
                                )
                        );
                    }

                    message.setToolsUsed(
                            traces
                    );

                    messages.add(
                            message
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load conversation messages.",
                    e
            );
        }

        return messages;
    }

    // =========================
    // Mapper
    // =========================
    private ChatMessage mapMessage(
            ResultSet resultSet
    ) throws Exception {

        ChatMessage message
                = new ChatMessage();

        message.setId(
                resultSet.getLong(
                        "id"
                )
        );

        message.setConversationId(
                resultSet.getLong(
                        "conversation_id"
                )
        );

        message.setRole(
                resultSet.getString(
                        "role"
                )
        );

        message.setContent(
                resultSet.getString(
                        "content"
                )
        );

        message.setCreatedAt(
                resultSet
                        .getTimestamp(
                                "created_at"
                        )
                        .toLocalDateTime()
        );

        return message;
    }
}
