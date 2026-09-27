package com.fangyao.agent;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JobAgentResultRepository {

    private final ObjectMapper objectMapper;

    public JobAgentResultRepository() {
        this.objectMapper = new ObjectMapper();
    }

    public void saveOrUpdate(
            long jobId,
            JobAnalysis analysis,
            MatchEvaluation evaluation,
            ApplicationStrategy strategy
    ) {

        String sql = """
                INSERT INTO job_agent_results (
                    job_id,
                    role_type,
                    seniority,
                    primary_skills,
                    secondary_skills,
                    required_years_experience,
                    analysis_summary,
                    overall_score,
                    skill_score,
                    experience_score,
                    role_fit_score,
                    strengths,
                    missing_skills,
                    recommendation,
                    priority,
                    resume_focus,
                    concerns,
                    application_advice,
                    updated_at
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (job_id)
                DO UPDATE SET
                    role_type = EXCLUDED.role_type,
                    seniority = EXCLUDED.seniority,
                    primary_skills = EXCLUDED.primary_skills,
                    secondary_skills = EXCLUDED.secondary_skills,
                    required_years_experience = EXCLUDED.required_years_experience,
                    analysis_summary = EXCLUDED.analysis_summary,
                    overall_score = EXCLUDED.overall_score,
                    skill_score = EXCLUDED.skill_score,
                    experience_score = EXCLUDED.experience_score,
                    role_fit_score = EXCLUDED.role_fit_score,
                    strengths = EXCLUDED.strengths,
                    missing_skills = EXCLUDED.missing_skills,
                    recommendation = EXCLUDED.recommendation,
                    priority = EXCLUDED.priority,
                    resume_focus = EXCLUDED.resume_focus,
                    concerns = EXCLUDED.concerns,
                    application_advice = EXCLUDED.application_advice,
                    updated_at = CURRENT_TIMESTAMP
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, jobId);

            // =========================
            // JobAnalysis
            // =========================
            if (analysis != null) {

                statement.setString(
                        2,
                        analysis.getRoleType()
                );

                statement.setString(
                        3,
                        analysis.getSeniority()
                );

                statement.setString(
                        4,
                        toJson(
                                analysis.getPrimarySkills()
                        )
                );

                statement.setString(
                        5,
                        toJson(
                                analysis.getSecondarySkills()
                        )
                );

                statement.setObject(
                        6,
                        analysis.getRequiredYearsExperience()
                );

                statement.setString(
                        7,
                        analysis.getSummary()
                );

            } else {

                statement.setString(2, null);
                statement.setString(3, null);
                statement.setString(4, null);
                statement.setString(5, null);
                statement.setObject(6, null);
                statement.setString(7, null);
            }

            // =========================
            // MatchEvaluation
            // =========================
            if (evaluation != null) {

                statement.setObject(
                        8,
                        evaluation.getOverallScore()
                );

                statement.setObject(
                        9,
                        evaluation.getSkillScore()
                );

                statement.setObject(
                        10,
                        evaluation.getExperienceScore()
                );

                statement.setObject(
                        11,
                        evaluation.getRoleFitScore()
                );

                statement.setString(
                        12,
                        toJson(
                                evaluation.getStrengths()
                        )
                );

                statement.setString(
                        13,
                        toJson(
                                evaluation.getMissingSkills()
                        )
                );

            } else {

                statement.setObject(8, null);
                statement.setObject(9, null);
                statement.setObject(10, null);
                statement.setObject(11, null);
                statement.setString(12, null);
                statement.setString(13, null);
            }

            // =========================
            // ApplicationStrategy
            // =========================
            if (strategy != null) {

                statement.setString(
                        14,
                        strategy.getRecommendation()
                );

                statement.setString(
                        15,
                        strategy.getPriority()
                );

                statement.setString(
                        16,
                        toJson(
                                strategy.getResumeFocus()
                        )
                );

                statement.setString(
                        17,
                        toJson(
                                strategy.getConcerns()
                        )
                );

                statement.setString(
                        18,
                        strategy.getApplicationAdvice()
                );

            } else {

                statement.setString(14, null);
                statement.setString(15, null);
                statement.setString(16, null);
                statement.setString(17, null);
                statement.setString(18, null);
            }

            statement.executeUpdate();

            System.out.println(
                    "[JobAgentResultRepository] Saved result for job ID: "
                            + jobId
            );

        } catch (Exception e) {

            System.err.println(
                    "[JobAgentResultRepository] Failed to save result for job ID: "
                            + jobId
            );

            e.printStackTrace();
        }
    }

    public JobAgentResult findByJobId(long jobId) {

        String sql = """
                SELECT
                    job_id,
                    role_type,
                    seniority,
                    primary_skills,
                    secondary_skills,
                    required_years_experience,
                    analysis_summary,
                    overall_score,
                    skill_score,
                    experience_score,
                    role_fit_score,
                    strengths,
                    missing_skills,
                    recommendation,
                    priority,
                    resume_focus,
                    concerns,
                    application_advice
                FROM job_agent_results
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

                JobAgentResult result =
                        new JobAgentResult();

                result.setJobId(
                        resultSet.getLong(
                                "job_id"
                        )
                );

                result.setRoleType(
                        resultSet.getString(
                                "role_type"
                        )
                );

                result.setSeniority(
                        resultSet.getString(
                                "seniority"
                        )
                );

                result.setPrimarySkills(
                        resultSet.getString(
                                "primary_skills"
                        )
                );

                result.setSecondarySkills(
                        resultSet.getString(
                                "secondary_skills"
                        )
                );

                result.setRequiredYearsExperience(
                        (Integer) resultSet.getObject(
                                "required_years_experience"
                        )
                );

                result.setAnalysisSummary(
                        resultSet.getString(
                                "analysis_summary"
                        )
                );

                result.setOverallScore(
                        (Integer) resultSet.getObject(
                                "overall_score"
                        )
                );

                result.setSkillScore(
                        (Integer) resultSet.getObject(
                                "skill_score"
                        )
                );

                result.setExperienceScore(
                        (Integer) resultSet.getObject(
                                "experience_score"
                        )
                );

                result.setRoleFitScore(
                        (Integer) resultSet.getObject(
                                "role_fit_score"
                        )
                );

                result.setStrengths(
                        resultSet.getString(
                                "strengths"
                        )
                );

                result.setMissingSkills(
                        resultSet.getString(
                                "missing_skills"
                        )
                );

                result.setRecommendation(
                        resultSet.getString(
                                "recommendation"
                        )
                );

                result.setPriority(
                        resultSet.getString(
                                "priority"
                        )
                );

                result.setResumeFocus(
                        resultSet.getString(
                                "resume_focus"
                        )
                );

                result.setConcerns(
                        resultSet.getString(
                                "concerns"
                        )
                );

                result.setApplicationAdvice(
                        resultSet.getString(
                                "application_advice"
                        )
                );

                return result;
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load agent result for job ID: "
                            + jobId,
                    e
            );
        }
    }

    private String toJson(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        try {

            return objectMapper
                    .writeValueAsString(value);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to convert value to JSON",
                    e
            );
        }
    }
}