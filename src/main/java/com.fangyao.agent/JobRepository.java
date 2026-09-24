package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JobRepository {

    public boolean save(Job job) {

        String sql =
                """
                INSERT INTO jobs (
                    external_id,
                    title,
                    company,
                    location,
                    url,
                    published_at,
                    description
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (external_id) DO NOTHING
                RETURNING id
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, job.getExternalId());
            statement.setString(2, job.getTitle());
            statement.setString(3, job.getCompany());
            statement.setString(4, job.getLocation());
            statement.setString(5, job.getUrl());

            if (job.getPublishedAt() != null) {
                statement.setObject(
                        6,
                        job.getPublishedAt()
                );
            } else {
                statement.setObject(
                        6,
                        null
                );
            }

            statement.setString(
                    7,
                    job.getDescription()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    int id =
                            resultSet.getInt("id");

                    job.setId(id);

                    return true;
                }
            }

            return false;

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] "
                            + e.getMessage()
            );

            return false;
        }
    }

    public boolean updateMatchResult(
            int jobId,
            JobMatchResult result
    ) {

        String sql =
                """
                UPDATE jobs
                SET
                    match_score = ?,
                    match_reason = ?,
                    match_gap = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    result.getScore()
            );

            statement.setString(
                    2,
                    result.getReason()
            );

            statement.setString(
                    3,
                    result.getGap()
            );

            statement.setInt(
                    4,
                    jobId
            );

            int rowsAffected =
                    statement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] "
                            + e.getMessage()
            );

            return false;
        }
    }
}