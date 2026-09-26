package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class JobRepository {

    public boolean updateJobStatus(int id, String status, boolean value) {
        String column;

        switch (status) {
            case "favorite":
                column = "favorite";
                break;
            case "applied":
                column = "applied";
                break;
            case "ignored":
                column = "ignored";
                break;
            default:
                return false;
        }

        String sql = "UPDATE jobs SET " + column + " = ? WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, value);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to update job status: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getStats() {

        Map<String, Object> stats = new HashMap<>();

        String sql = """
            SELECT
                COUNT(*) AS total_jobs,
                COUNT(*) FILTER (WHERE match_score >= 70) AS high_match_jobs,
                COUNT(*) FILTER (WHERE match_score IS NULL) AS unscored_jobs,
                AVG(match_score) FILTER (WHERE match_score IS NOT NULL) AS average_score
            FROM jobs
            """;

        try (
                Connection connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {

                stats.put(
                        "totalJobs",
                        resultSet.getInt("total_jobs")
                );

                stats.put(
                        "highMatchJobs",
                        resultSet.getInt("high_match_jobs")
                );

                stats.put(
                        "unscoredJobs",
                        resultSet.getInt("unscored_jobs")
                );

                double averageScore
                        = resultSet.getDouble("average_score");

                if (resultSet.wasNull()) {
                    stats.put("averageScore", 0);
                } else {
                    stats.put(
                            "averageScore",
                            Math.round(averageScore * 10.0) / 10.0
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] Failed to load stats: "
                    + e.getMessage()
            );
        }

        return stats;
    }

    public List<Job> getAllJobs() {

        List<Job> jobs = new ArrayList<>();

        String sql = """
            SELECT
                id,
                external_id,
                title,
                company,
                location,
                url,
                published_at,
                description,
                match_score,
                match_reason,
                match_gap,
                favorite,
                applied,
                ignored

            FROM jobs
            ORDER BY first_seen_at DESC
            """;

        try (
                Connection connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Job job = new Job(
                        resultSet.getString("external_id"),
                        resultSet.getString("title"),
                        resultSet.getString("company"),
                        resultSet.getString("location"),
                        resultSet.getString("url"),
                        resultSet.getTimestamp("published_at") != null
                        ? resultSet.getTimestamp("published_at").toLocalDateTime()
                        : null,
                        resultSet.getString("description")
                );

                job.setFavorite(resultSet.getBoolean("favorite"));
                job.setApplied(resultSet.getBoolean("applied"));
                job.setIgnored(resultSet.getBoolean("ignored"));

                job.setId(resultSet.getInt("id"));

                int score = resultSet.getInt("match_score");
                if (!resultSet.wasNull()) {
                    job.setMatchScore(score);
                }

                job.setMatchReason(
                        resultSet.getString("match_reason")
                );

                job.setMatchGap(
                        resultSet.getString("match_gap")
                );

                jobs.add(job);
            }

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] Failed to load jobs: "
                    + e.getMessage()
            );
        }

        return jobs;
    }

    public List<Job> getHighMatchJobs() {

        List<Job> jobs = new ArrayList<>();

        String sql = """
            SELECT
                id,
                external_id,
                title,
                company,
                location,
                url,
                published_at,
                description,
                match_score,
                match_reason,
                match_gap,
                favorite,
                applied,
                ignored

            FROM jobs
            WHERE match_score >= 70
            ORDER BY match_score DESC, first_seen_at DESC
            """;

        try (
                Connection connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Job job = new Job(
                        resultSet.getString("external_id"),
                        resultSet.getString("title"),
                        resultSet.getString("company"),
                        resultSet.getString("location"),
                        resultSet.getString("url"),
                        resultSet.getTimestamp("published_at") != null
                        ? resultSet.getTimestamp("published_at").toLocalDateTime()
                        : null,
                        resultSet.getString("description")
                );

                job.setId(resultSet.getInt("id"));
                job.setMatchScore(resultSet.getInt("match_score"));
                job.setMatchReason(resultSet.getString("match_reason"));
                job.setMatchGap(resultSet.getString("match_gap"));
                job.setFavorite(resultSet.getBoolean("favorite"));
                job.setApplied(resultSet.getBoolean("applied"));
                job.setIgnored(resultSet.getBoolean("ignored"));

                jobs.add(job);
            }

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] Failed to load high-match jobs: "
                    + e.getMessage()
            );
        }

        return jobs;
    }

    public Job getJobById(int id) {

        String sql = """
            SELECT
                id,
                external_id,
                title,
                company,
                location,
                url,
                published_at,
                description,
                match_score,
                match_reason,
                match_gap,
                favorite,
                applied,
                ignored

            FROM jobs
            WHERE id = ?
            """;

        try (
                Connection connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Job job = new Job(
                            resultSet.getString("external_id"),
                            resultSet.getString("title"),
                            resultSet.getString("company"),
                            resultSet.getString("location"),
                            resultSet.getString("url"),
                            resultSet.getTimestamp("published_at") != null
                            ? resultSet.getTimestamp("published_at").toLocalDateTime()
                            : null,
                            resultSet.getString("description")
                    );

                    job.setId(resultSet.getInt("id"));

                    int score = resultSet.getInt("match_score");
                    if (!resultSet.wasNull()) {
                        job.setMatchScore(score);
                    }

                    job.setMatchReason(resultSet.getString("match_reason"));
                    job.setMatchGap(resultSet.getString("match_gap"));
                    job.setFavorite(resultSet.getBoolean("favorite"));
                    job.setApplied(resultSet.getBoolean("applied"));
                    job.setIgnored(resultSet.getBoolean("ignored"));

                    return job;
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "[Database Error] Failed to load job by id: "
                    + e.getMessage()
            );
        }

        return null;
    }

    public boolean save(Job job) {

        String sql
                = """
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
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

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
                    ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {

                    int id
                            = resultSet.getInt("id");

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

        String sql
                = """
                UPDATE jobs
                SET
                    match_score = ?,
                    match_reason = ?,
                    match_gap = ?
                WHERE id = ?
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

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

            int rowsAffected
                    = statement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {

            System.out.println(
                    "[Database Error] "
                    + e.getMessage()
            );

            return false;
        }
    }

    public List<Job> getUnscoredJobs() {

        List<Job> jobs = new ArrayList<>();

        String sql = """
            SELECT
                id,
                external_id,
                title,
                company,
                location,
                url,
                published_at,
                description,
                match_score,
                match_reason,
                match_gap
            FROM jobs
            WHERE match_score IS NULL
            ORDER BY first_seen_at ASC
            LIMIT 20
            """;

        try (Connection connection = DatabaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Job job = new Job(
                        resultSet.getString("external_id"),
                        resultSet.getString("title"),
                        resultSet.getString("company"),
                        resultSet.getString("location"),
                        resultSet.getString("url"),
                        resultSet.getTimestamp("published_at") != null
                        ? resultSet.getTimestamp("published_at").toLocalDateTime()
                        : null,
                        resultSet.getString("description")
                );

                job.setId(resultSet.getInt("id"));

                jobs.add(job);
            }

        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to load unscored jobs.");
            e.printStackTrace();
        }

        return jobs;
    }
}
