package com.fangyao.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CandidateProfileRepository {

    private final ObjectMapper objectMapper;

    public CandidateProfileRepository() {
        this.objectMapper = new ObjectMapper();
    }

    public CandidateProfile loadProfile() {

        String sql = """
                SELECT
                    id,
                    name,
                    languages,
                    backend_technologies,
                    cloud_technologies,
                    databases,
                    frontend_technologies,
                    tools,
                    target_roles,
                    experience_highlights,
                    project_highlights,
                    preferred_locations,
                    preferred_work_modes
                FROM candidate_profile
                ORDER BY id
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

            if (!resultSet.next()) {
                return null;
            }

            CandidateProfile profile =
                    new CandidateProfile();

            profile.setId(
                    resultSet.getLong("id")
            );

            profile.setName(
                    resultSet.getString("name")
            );

            profile.setLanguages(
                    parseList(
                            resultSet.getString("languages")
                    )
            );

            profile.setBackendTechnologies(
                    parseList(
                            resultSet.getString(
                                    "backend_technologies"
                            )
                    )
            );

            profile.setCloudTechnologies(
                    parseList(
                            resultSet.getString(
                                    "cloud_technologies"
                            )
                    )
            );

            profile.setDatabases(
                    parseList(
                            resultSet.getString("databases")
                    )
            );

            profile.setFrontendTechnologies(
                    parseList(
                            resultSet.getString(
                                    "frontend_technologies"
                            )
                    )
            );

            profile.setTools(
                    parseList(
                            resultSet.getString("tools")
                    )
            );

            profile.setTargetRoles(
                    parseList(
                            resultSet.getString(
                                    "target_roles"
                            )
                    )
            );

            profile.setExperienceHighlights(
                    parseList(
                            resultSet.getString(
                                    "experience_highlights"
                            )
                    )
            );

            profile.setProjectHighlights(
                    parseList(
                            resultSet.getString(
                                    "project_highlights"
                            )
                    )
            );

            profile.setPreferredLocations(
                    parseList(
                            resultSet.getString(
                                    "preferred_locations"
                            )
                    )
            );

            profile.setPreferredWorkModes(
                    parseList(
                            resultSet.getString(
                                    "preferred_work_modes"
                            )
                    )
            );

            return profile;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to load candidate profile.",
                    e
            );
        }
    }

    public CandidateProfile updateProfile(
            CandidateProfile profile
    ) {

        if (profile == null) {

            throw new IllegalArgumentException(
                    "Candidate profile cannot be null."
            );
        }

        if (profile.getId() == null) {

            throw new IllegalArgumentException(
                    "Candidate profile ID is required for update."
            );
        }

        String sql = """
                UPDATE candidate_profile
                SET
                    name = ?,
                    languages = ?,
                    backend_technologies = ?,
                    cloud_technologies = ?,
                    databases = ?,
                    frontend_technologies = ?,
                    tools = ?,
                    target_roles = ?,
                    experience_highlights = ?,
                    project_highlights = ?,
                    preferred_locations = ?,
                    preferred_work_modes = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    profile.getName()
            );

            statement.setString(
                    2,
                    toJson(
                            profile.getLanguages()
                    )
            );

            statement.setString(
                    3,
                    toJson(
                            profile.getBackendTechnologies()
                    )
            );

            statement.setString(
                    4,
                    toJson(
                            profile.getCloudTechnologies()
                    )
            );

            statement.setString(
                    5,
                    toJson(
                            profile.getDatabases()
                    )
            );

            statement.setString(
                    6,
                    toJson(
                            profile.getFrontendTechnologies()
                    )
            );

            statement.setString(
                    7,
                    toJson(
                            profile.getTools()
                    )
            );

            statement.setString(
                    8,
                    toJson(
                            profile.getTargetRoles()
                    )
            );

            statement.setString(
                    9,
                    toJson(
                            profile.getExperienceHighlights()
                    )
            );

            statement.setString(
                    10,
                    toJson(
                            profile.getProjectHighlights()
                    )
            );

            statement.setString(
                    11,
                    toJson(
                            profile.getPreferredLocations()
                    )
            );

            statement.setString(
                    12,
                    toJson(
                            profile.getPreferredWorkModes()
                    )
            );

            statement.setLong(
                    13,
                    profile.getId()
            );

            int updatedRows =
                    statement.executeUpdate();

            if (updatedRows == 0) {

                throw new IllegalStateException(
                        "Candidate profile not found for ID: "
                                + profile.getId()
                );
            }

            return loadProfile();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update candidate profile.",
                    e
            );
        }
    }

    private List<String> parseList(
            String json
    ) {

        if (
                json == null
                        || json.isBlank()
        ) {

            return List.of();
        }

        try {

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {}
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse candidate profile list: "
                            + json,
                    e
            );
        }
    }

    private String toJson(
            List<String> values
    ) {

        try {

            return objectMapper.writeValueAsString(
                    values == null
                            ? List.of()
                            : values
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to serialize candidate profile list.",
                    e
            );
        }
    }
}