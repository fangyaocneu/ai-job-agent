package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class AgentRunRepository {

    public long startRun(
            long jobId,
            String agentName
    ) {

        String sql = """
                INSERT INTO agent_runs (
                    job_id,
                    agent_name,
                    status,
                    started_at
                )
                VALUES (
                    ?, ?, ?, CURRENT_TIMESTAMP
                )
                RETURNING id
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    jobId
            );

            statement.setString(
                    2,
                    agentName
            );

            statement.setString(
                    3,
                    "RUNNING"
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong(
                            "id"
                    );
                }
            }

            throw new RuntimeException(
                    "Failed to create agent run."
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to start agent run for job ID: "
                    + jobId
                    + ", agent: "
                    + agentName,
                    e
            );
        }
    }

    public void completeRun(
            long runId
    ) {

        String sql = """
                UPDATE agent_runs
                SET
                    status = ?,
                    completed_at = CURRENT_TIMESTAMP,
                    duration_ms =
                        EXTRACT(
                            EPOCH FROM (
                                CURRENT_TIMESTAMP
                                - started_at
                            )
                        ) * 1000
                WHERE id = ?
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    "SUCCESS"
            );

            statement.setLong(
                    2,
                    runId
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to complete agent run ID: "
                    + runId,
                    e
            );
        }
    }

    public void failRun(
            long runId,
            String errorMessage
    ) {

        String sql = """
                UPDATE agent_runs
                SET
                    status = ?,
                    completed_at = CURRENT_TIMESTAMP,
                    duration_ms =
                        EXTRACT(
                            EPOCH FROM (
                                CURRENT_TIMESTAMP
                                - started_at
                            )
                        ) * 1000,
                    error_message = ?
                WHERE id = ?
                """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    "FAILED"
            );

            statement.setString(
                    2,
                    errorMessage
            );

            statement.setLong(
                    3,
                    runId
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to mark agent run as failed. ID: "
                    + runId,
                    e
            );
        }
    }

    public List<AgentRun> findRecentRuns() {

        String sql = """
                SELECT
                    id,
                    job_id,
                    agent_name,
                    status,
                    started_at,
                    completed_at,
                    duration_ms,
                    error_message
                FROM agent_runs
                ORDER BY started_at DESC
                LIMIT 100
                """;

        List<AgentRun> runs
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet resultSet
                = statement.executeQuery()) {

            while (resultSet.next()) {

                AgentRun run
                        = mapRow(
                                resultSet
                        );

                runs.add(run);
            }

            return runs;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load recent agent runs.",
                    e
            );
        }
    }

    public List<AgentRun> findRunsByJobId(
            long jobId
    ) {

        String sql = """
                SELECT
                    id,
                    job_id,
                    agent_name,
                    status,
                    started_at,
                    completed_at,
                    duration_ms,
                    error_message
                FROM agent_runs
                WHERE job_id = ?
                ORDER BY started_at ASC
                """;

        List<AgentRun> runs
                = new ArrayList<>();

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    jobId
            );

            try (
                    ResultSet resultSet
                    = statement.executeQuery()) {

                while (resultSet.next()) {

                    runs.add(
                            mapRow(
                                    resultSet
                            )
                    );
                }
            }

            return runs;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load agent runs for job ID: "
                    + jobId,
                    e
            );
        }
    }

    private AgentRun mapRow(
            ResultSet resultSet
    ) throws Exception {

        AgentRun run
                = new AgentRun();

        run.setId(
                resultSet.getLong(
                        "id"
                )
        );

        run.setJobId(
                resultSet.getLong(
                        "job_id"
                )
        );

        run.setAgentName(
                resultSet.getString(
                        "agent_name"
                )
        );

        run.setStatus(
                resultSet.getString(
                        "status"
                )
        );

        Timestamp startedTimestamp
                = resultSet.getTimestamp(
                        "started_at"
                );

        if (startedTimestamp != null) {

            run.setStartedAt(
                    startedTimestamp
                            .toLocalDateTime()
            );
        }

        Timestamp completedTimestamp
                = resultSet.getTimestamp(
                        "completed_at"
                );

        if (completedTimestamp != null) {

            run.setCompletedAt(
                    completedTimestamp
                            .toLocalDateTime()
            );
        }

        Object duration
                = resultSet.getObject(
                        "duration_ms"
                );

        if (duration != null) {

            run.setDurationMs(
                    ((Number) duration)
                            .longValue()
            );
        }

        run.setErrorMessage(
                resultSet.getString(
                        "error_message"
                )
        );

        return run;
    }

    public void markSuccess(
            long runId,
            long durationMs
    ) {

        String sql = """
            UPDATE agent_runs
            SET
                status = ?,
                completed_at = CURRENT_TIMESTAMP,
                duration_ms = ?,
                error_message = NULL
            WHERE id = ?
            """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    "SUCCESS"
            );

            statement.setLong(
                    2,
                    durationMs
            );

            statement.setLong(
                    3,
                    runId
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to mark agent run as successful. ID: "
                    + runId,
                    e
            );
        }
    }

    public void markFailed(
            long runId,
            long durationMs,
            String errorMessage
    ) {

        String sql = """
            UPDATE agent_runs
            SET
                status = ?,
                completed_at = CURRENT_TIMESTAMP,
                duration_ms = ?,
                error_message = ?
            WHERE id = ?
            """;

        try (
                Connection connection
                = DatabaseConfig.getConnection(); PreparedStatement statement
                = connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    "FAILED"
            );

            statement.setLong(
                    2,
                    durationMs
            );

            statement.setString(
                    3,
                    errorMessage
            );

            statement.setLong(
                    4,
                    runId
            );

            statement.executeUpdate();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to mark agent run as failed. ID: "
                    + runId,
                    e
            );
        }
    }
}
