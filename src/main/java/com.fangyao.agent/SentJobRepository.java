package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SentJobRepository {

    private static final int MIN_MATCH_SCORE = 70;

    public boolean isSent(int jobId) {

        String sql =
                """
                SELECT 1
                FROM sent_jobs
                WHERE job_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, jobId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                return resultSet.next();
            }

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] "
                            + e.getMessage()
            );

            return false;
        }
    }

    public boolean markAsSent(int jobId) {

        String sql =
                """
                INSERT INTO sent_jobs (job_id)
                VALUES (?)
                ON CONFLICT (job_id) DO NOTHING
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
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

    public List<Job> getUnsentJobs() {

        List<Job> jobs =
                new ArrayList<>();

        String sql =
                """
                SELECT
                    j.id,
                    j.external_id,
                    j.title,
                    j.company,
                    j.location,
                    j.url,
                    j.published_at,
                    j.description,
                    j.match_score,
                    j.match_reason,
                    j.match_gap
                FROM jobs j
                LEFT JOIN sent_jobs s
                    ON j.id = s.job_id
                WHERE
                    s.job_id IS NULL
                    AND j.match_score IS NOT NULL
                    AND j.match_score >= ?
                ORDER BY
                    j.match_score DESC,
                    j.first_seen_at DESC
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    MIN_MATCH_SCORE
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Job job =
                            new Job(
                                    resultSet.getString(
                                            "external_id"
                                    ),
                                    resultSet.getString(
                                            "title"
                                    ),
                                    resultSet.getString(
                                            "company"
                                    ),
                                    resultSet.getString(
                                            "location"
                                    ),
                                    resultSet.getString(
                                            "url"
                                    ),
                                    resultSet.getTimestamp(
                                            "published_at"
                                    ) != null
                                            ? resultSet
                                                    .getTimestamp(
                                                            "published_at"
                                                    )
                                                    .toLocalDateTime()
                                            : null,
                                    resultSet.getString(
                                            "description"
                                    )
                            );

                    job.setId(
                            resultSet.getInt(
                                    "id"
                            )
                    );

                    job.setMatchScore(
                            resultSet.getInt(
                                    "match_score"
                            )
                    );

                    job.setMatchReason(
                            resultSet.getString(
                                    "match_reason"
                            )
                    );

                    job.setMatchGap(
                            resultSet.getString(
                                    "match_gap"
                            )
                    );

                    jobs.add(job);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] "
                            + e.getMessage()
            );
        }

        return jobs;
    }
}