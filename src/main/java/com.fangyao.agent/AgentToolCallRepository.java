package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgentToolCallRepository {

    public void save(AgentToolCall toolCall) {

        String sql = """
                INSERT INTO agent_tool_calls
                (message_id, tool_name, arguments, result)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    toolCall.getMessageId()
            );

            statement.setString(
                    2,
                    toolCall.getToolName()
            );

            statement.setString(
                    3,
                    toolCall.getArguments()
            );

            statement.setString(
                    4,
                    toolCall.getResult()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save agent tool call",
                    e
            );
        }
    }

    public List<AgentToolCall> findByMessageId(
            Long messageId) {

        String sql = """
                SELECT
                    id,
                    message_id,
                    tool_name,
                    arguments,
                    result
                FROM agent_tool_calls
                WHERE message_id = ?
                ORDER BY id ASC
                """;

        List<AgentToolCall> toolCalls =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    messageId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    AgentToolCall toolCall =
                            new AgentToolCall();

                    toolCall.setId(
                            resultSet.getLong("id")
                    );

                    toolCall.setMessageId(
                            resultSet.getLong("message_id")
                    );

                    toolCall.setToolName(
                            resultSet.getString("tool_name")
                    );

                    toolCall.setArguments(
                            resultSet.getString("arguments")
                    );

                    toolCall.setResult(
                            resultSet.getString("result")
                    );

                    toolCalls.add(
                            toolCall
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load agent tool calls",
                    e
            );
        }

        return toolCalls;
    }
}