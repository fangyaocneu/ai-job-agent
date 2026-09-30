package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResumeRepository {

    public long save(
            String fileName,
            String content
    ) {

        String sql = """
                INSERT INTO resumes (
                    file_name,
                    content
                )
                VALUES (?, ?)
                RETURNING id
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    fileName
            );

            statement.setString(
                    2,
                    content
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to save resume.",
                    e
            );
        }

        throw new IllegalStateException(
                "Resume was not saved."
        );
    }

    public String getLatestContent() {

        String sql = """
                SELECT content
                FROM resumes
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            if (resultSet.next()) {
                return resultSet.getString(
                        "content"
                );
            }

            return null;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load latest resume.",
                    e
            );
        }
    }
}