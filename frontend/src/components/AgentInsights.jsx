import { useEffect, useState } from "react";

function parseList(value) {
    if (!value) {
        return [];
    }

    try {
        const parsed = JSON.parse(value);

        return Array.isArray(parsed)
            ? parsed
            : [];
    } catch {
        return [];
    }
}

function ScoreItem({ label, score }) {
    return (
        <div className="agent-score-item">
            <span className="agent-score-label">
                {label}
            </span>

            <span className="agent-score-value">
                {score ?? "-"}
            </span>
        </div>
    );
}

function AgentInsights({ jobId }) {

    const [insights, setInsights] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState(null);

    useEffect(() => {

        if (!jobId) {
            return;
        }

        async function loadInsights() {

            setLoading(true);
            setError(null);

            try {

                const response =
                    await fetch(
                        `http://localhost:8080/api/jobs/${jobId}/agent-insights`
                    );

                if (response.status === 404) {

                    setInsights(null);
                    return;
                }

                if (!response.ok) {

                    throw new Error(
                        `HTTP ${response.status}`
                    );
                }

                const data =
                    await response.json();

                setInsights(data);

            } catch (err) {

                console.error(
                    "Failed to load agent insights:",
                    err
                );

                setError(
                    "Unable to load AI insights."
                );

            } finally {

                setLoading(false);
            }
        }

        loadInsights();

    }, [jobId]);

    if (loading) {

        return (
            <div className="agent-insights-card">
                Loading AI insights...
            </div>
        );
    }

    if (error) {

        return (
            <div className="agent-insights-card">
                {error}
            </div>
        );
    }

    if (!insights) {

        return (
            <div className="agent-insights-card">
                No AI analysis available yet.
            </div>
        );
    }

    const primarySkills =
        parseList(
            insights.primarySkills
        );

    const secondarySkills =
        parseList(
            insights.secondarySkills
        );

    const strengths =
        parseList(
            insights.strengths
        );

    const missingSkills =
        parseList(
            insights.missingSkills
        );

    const resumeFocus =
        parseList(
            insights.resumeFocus
        );

    const concerns =
        parseList(
            insights.concerns
        );

    return (
        <div className="agent-insights-card">

            <div className="agent-insights-header">

                <div>
                    <h2>
                        AI Insights
                    </h2>

                    <div className="agent-role-info">

                        {insights.roleType && (
                            <span>
                                {insights.roleType}
                            </span>
                        )}

                        {insights.seniority && (
                            <span>
                                {insights.seniority}
                            </span>
                        )}

                    </div>
                </div>

                {insights.overallScore != null && (
                    <div className="overall-score">

                        <span className="overall-score-number">
                            {insights.overallScore}
                        </span>

                        <span className="overall-score-label">
                            Match
                        </span>

                    </div>
                )}

            </div>

            <div className="agent-score-grid">

                <ScoreItem
                    label="Skill"
                    score={insights.skillScore}
                />

                <ScoreItem
                    label="Experience"
                    score={insights.experienceScore}
                />

                <ScoreItem
                    label="Role Fit"
                    score={insights.roleFitScore}
                />

                <ScoreItem
                    label="Years Required"
                    score={
                        insights.requiredYearsExperience
                    }
                />

            </div>

            {insights.analysisSummary && (
                <section className="agent-section">

                    <h3>
                        Role Analysis
                    </h3>

                    <p>
                        {insights.analysisSummary}
                    </p>

                </section>
            )}

            {primarySkills.length > 0 && (
                <section className="agent-section">

                    <h3>
                        Primary Skills
                    </h3>

                    <div className="skill-tags">

                        {primarySkills.map(
                            (skill, index) => (

                                <span
                                    key={index}
                                    className="skill-tag"
                                >
                                    {skill}
                                </span>

                            )
                        )}

                    </div>

                </section>
            )}

            {strengths.length > 0 && (
                <section className="agent-section">

                    <h3>
                        Strengths
                    </h3>

                    <ul>

                        {strengths.map(
                            (item, index) => (

                                <li key={index}>
                                    {item}
                                </li>

                            )
                        )}

                    </ul>

                </section>
            )}

            {missingSkills.length > 0 && (
                <section className="agent-section">

                    <h3>
                        Missing Skills
                    </h3>

                    <ul>

                        {missingSkills.map(
                            (item, index) => (

                                <li key={index}>
                                    {item}
                                </li>

                            )
                        )}

                    </ul>

                </section>
            )}

            {insights.recommendation && (
                <section className="agent-section strategy-section">

                    <div className="strategy-header">

                        <h3>
                            Application Strategy
                        </h3>

                        <div className="strategy-badges">

                            <span className="recommendation-badge">
                                {insights.recommendation}
                            </span>

                            {insights.priority && (
                                <span className="priority-badge">
                                    {insights.priority}
                                </span>
                            )}

                        </div>

                    </div>

                    {resumeFocus.length > 0 && (
                        <>
                            <h4>
                                Resume Focus
                            </h4>

                            <ul>

                                {resumeFocus.map(
                                    (item, index) => (

                                        <li key={index}>
                                            {item}
                                        </li>

                                    )
                                )}

                            </ul>
                        </>
                    )}

                    {concerns.length > 0 && (
                        <>
                            <h4>
                                Concerns
                            </h4>

                            <ul>

                                {concerns.map(
                                    (item, index) => (

                                        <li key={index}>
                                            {item}
                                        </li>

                                    )
                                )}

                            </ul>
                        </>
                    )}

                    {insights.applicationAdvice && (
                        <>
                            <h4>
                                Advice
                            </h4>

                            <p>
                                {
                                    insights.applicationAdvice
                                }
                            </p>
                        </>
                    )}

                </section>
            )}

            {secondarySkills.length > 0 && (
                <section className="agent-section">

                    <h3>
                        Secondary Skills
                    </h3>

                    <div className="skill-tags">

                        {secondarySkills.map(
                            (skill, index) => (

                                <span
                                    key={index}
                                    className="skill-tag"
                                >
                                    {skill}
                                </span>

                            )
                        )}

                    </div>

                </section>
            )}

        </div>
    );
}

export default AgentInsights;