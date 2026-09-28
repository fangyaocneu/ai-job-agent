CREATE TABLE IF NOT EXISTS job_agent_results (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL UNIQUE,

    role_type VARCHAR(50),
    seniority VARCHAR(50),

    primary_skills TEXT,
    secondary_skills TEXT,
    required_years_experience INTEGER,
    analysis_summary TEXT,

    overall_score INTEGER,
    skill_score INTEGER,
    experience_score INTEGER,
    role_fit_score INTEGER,

    strengths TEXT,
    missing_skills TEXT,

    recommendation VARCHAR(50),
    priority VARCHAR(50),

    resume_focus TEXT,
    concerns TEXT,
    application_advice TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_job_agent_results_job
        FOREIGN KEY(job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS agent_runs (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,

    agent_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,

    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms BIGINT,

    error_message TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_agent_runs_job
        FOREIGN KEY(job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_agent_runs_job_id
    ON agent_runs(job_id);


CREATE INDEX IF NOT EXISTS idx_agent_runs_agent_name
    ON agent_runs(agent_name);


CREATE INDEX IF NOT EXISTS idx_agent_runs_status
    ON agent_runs(status);


CREATE INDEX IF NOT EXISTS idx_agent_runs_created_at
    ON agent_runs(created_at DESC);


CREATE INDEX IF NOT EXISTS idx_job_agent_results_overall_score
    ON job_agent_results(overall_score DESC);