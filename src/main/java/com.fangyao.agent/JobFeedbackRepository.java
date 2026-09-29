package com.fangyao.agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JobFeedbackRepository {

    public JobFeedback saveOrUpdate(
            long jobId,
            JobFeedback feedback
    ) {

        String sql = """
                INSERT INTO job_feedback (
                    job_id,
                    feedback_label,
                    reason,
                    comment
                )
                VALUES (?, ?, ?, ?)

                ON CONFLICT (job_id)
                DO UPDATE SET
                    feedback_label = EXCLUDED.feedback_label,
                    reason = EXCLUDED.reason,
                    comment = EXCLUDED.comment,
                    updated_at = CURRENT_TIMESTAMP

                RETURNING
                    id,
                    job_id,
                    feedback_label,
                    reason,
                    comment,
                    created_at,
                    updated_at
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    jobId
            );

            statement.setString(
                    2,
                    feedback.getFeedbackLabel()
            );

            statement.setString(
                    3,
                    feedback.getReason()
            );

            statement.setString(
                    4,
                    feedback.getComment()
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapFeedback(
                        resultSet
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to save feedback for job ID: "
                            + jobId,
                    e
            );
        }
    }

    public JobFeedback findByJobId(
            long jobId
    ) {

        String sql = """
                SELECT
                    id,
                    job_id,
                    feedback_label,
                    reason,
                    comment,
                    created_at,
                    updated_at

                FROM job_feedback

                WHERE job_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    jobId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    return null;
                }

                return mapFeedback(
                        resultSet
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load feedback for job ID: "
                            + jobId,
                    e
            );
        }
    }

    private JobFeedback mapFeedback(
            ResultSet resultSet
    ) throws Exception {

        JobFeedback feedback =
                new JobFeedback();

        feedback.setId(
                resultSet.getLong(
                        "id"
                )
        );

        feedback.setJobId(
                resultSet.getLong(
                        "job_id"
                )
        );

        feedback.setFeedbackLabel(
                resultSet.getString(
                        "feedback_label"
                )
        );

        feedback.setReason(
                resultSet.getString(
                        "reason"
                )
        );

        feedback.setComment(
                resultSet.getString(
                        "comment"
                )
        );

        if (
                resultSet.getTimestamp(
                        "created_at"
                ) != null
        ) {

            feedback.setCreatedAt(
                    resultSet
                            .getTimestamp(
                                    "created_at"
                            )
                            .toLocalDateTime()
            );
        }

        if (
                resultSet.getTimestamp(
                        "updated_at"
                ) != null
        ) {

            feedback.setUpdatedAt(
                    resultSet
                            .getTimestamp(
                                    "updated_at"
                            )
                            .toLocalDateTime()
            );
        }

        return feedback;
    }
}