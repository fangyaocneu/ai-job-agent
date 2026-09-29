package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationRepository {

    public List<ChatConversation> getAll() {

        String sql = """
                SELECT
                    id,
                    title,
                    created_at,
                    updated_at
                FROM chat_conversations
                ORDER BY updated_at DESC, id DESC
                """;

        List<ChatConversation> conversations
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            while (resultSet.next()) {

                conversations.add(
                        mapConversation(
                                resultSet
                        )
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load conversations.",
                    e
            );
        }

        return conversations;
    }

    public ChatConversation create(
            String title
    ) {

        String sql = """
                INSERT INTO chat_conversations (
                    title
                )
                VALUES (?)
                RETURNING
                    id,
                    title,
                    created_at,
                    updated_at
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    title
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapConversation(
                            resultSet
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create conversation.",
                    e
            );
        }

        throw new IllegalStateException(
                "Conversation was not created."
        );
    }

    private ChatConversation mapConversation(
            ResultSet resultSet
    ) throws Exception {

        ChatConversation conversation
                = new ChatConversation();

        conversation.setId(
                resultSet.getLong(
                        "id"
                )
        );

        conversation.setTitle(
                resultSet.getString(
                        "title"
                )
        );

        conversation.setCreatedAt(
                resultSet
                        .getTimestamp(
                                "created_at"
                        )
                        .toLocalDateTime()
        );

        conversation.setUpdatedAt(
                resultSet
                        .getTimestamp(
                                "updated_at"
                        )
                        .toLocalDateTime()
        );

        return conversation;
    }

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
    public void delete(
        long conversationId
) {

    String sql = """
            DELETE FROM chat_conversations
            WHERE id = ?
            """;

    try (
            Connection connection =
                    DatabaseConfig.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
    ) {

        statement.setLong(
                1,
                conversationId
        );

        statement.executeUpdate();

    } catch (
            Exception e
    ) {

        throw new RuntimeException(
                "Failed to delete conversation.",
                e
        );
    }
}
}


                 
                  
                  
                 
                  
                 
                     
                 
