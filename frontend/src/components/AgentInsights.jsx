import { useEffect, useState } from "react";

function parseList(value) {

    if (!value) {
        return [];
    }

    try {

        const parsed =
            JSON.parse(value);

        return Array.isArray(parsed)
            ? parsed
            : [];

    } catch {
        return [];
    }
}

function ScoreItem({
    label,
    score,
    weight
}) {

    return (
        <div className="agent-score-item">

            <span className="agent-score-label">
                {label}

                {weight && (
                    <small
                        style={{
                            marginLeft: "6px",
                            opacity: 0.6
                        }}
                    >
                        {weight}
                    </small>
                )}
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

    // =========================
    // Feedback State
    // =========================

    const [feedbackLabel, setFeedbackLabel] =
        useState("");

    const [feedbackReason, setFeedbackReason] =
        useState("");

    const [feedbackComment, setFeedbackComment] =
        useState("");

    const [feedbackSaving, setFeedbackSaving] =
        useState(false);

    const [feedbackMessage, setFeedbackMessage] =
        useState("");

    // =========================
    // Load AI Insights
    // =========================

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
                        `/api/jobs/${jobId}/agent-insights`
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

    // =========================
    // Load Existing Feedback
    // =========================

    useEffect(() => {

        if (!jobId) {
            return;
        }

        async function loadFeedback() {

            try {

                const response =
                    await fetch(
                        `/api/jobs/${jobId}/feedback`
                    );

                if (response.status === 404) {

                    setFeedbackLabel("");
                    setFeedbackReason("");
                    setFeedbackComment("");

                    return;
                }

                if (!response.ok) {

                    throw new Error(
                        `HTTP ${response.status}`
                    );
                }

                const data =
                    await response.json();

                setFeedbackLabel(
                    data.feedbackLabel || ""
                );

                setFeedbackReason(
                    data.reason || ""
                );

                setFeedbackComment(
                    data.comment || ""
                );

            } catch (err) {

                console.error(
                    "Failed to load feedback:",
                    err
                );
            }
        }

        loadFeedback();

    }, [jobId]);

    // =========================
    // Save Feedback
    // =========================

    async function saveFeedback() {

        if (!feedbackLabel) {

            setFeedbackMessage(
                "Please choose Like or Dislike."
            );

            return;
        }

        try {

            setFeedbackSaving(true);
            setFeedbackMessage("");

            const response =
                await fetch(
                    `/api/jobs/${jobId}/feedback`,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify({
                                feedbackLabel,
                                reason:
                                    feedbackReason || null,
                                comment:
                                    feedbackComment || null
                            })
                    }
                );

            if (!response.ok) {

                throw new Error(
                    `HTTP ${response.status}`
                );
            }

            const saved =
                await response.json();

            setFeedbackLabel(
                saved.feedbackLabel || ""
            );

            setFeedbackReason(
                saved.reason || ""
            );

            setFeedbackComment(
                saved.comment || ""
            );

            setFeedbackMessage(
                "Feedback saved."
            );

        } catch (err) {

            console.error(
                "Failed to save feedback:",
                err
            );

            setFeedbackMessage(
                "Unable to save feedback."
            );

        } finally {

            setFeedbackSaving(false);
        }
    }

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

    const displayScore =
        insights.finalScore
        ?? insights.overallScore;

    const likeReasons = [
        ["GOOD_ROLE", "Good Role"],
        ["GOOD_TECH_STACK", "Good Tech Stack"],
        ["GOOD_LOCATION", "Good Location"],
        ["GOOD_WORK_MODE", "Good Work Mode"]
    ];

    const dislikeReasons = [
        ["WRONG_ROLE", "Wrong Role"],
        ["WRONG_LOCATION", "Wrong Location"],
        ["TOO_SENIOR", "Too Senior"],
        ["MISSING_SKILLS", "Missing Skills"],
        ["NOT_INTERESTED", "Not Interested"]
    ];

    const reasonOptions =
        feedbackLabel === "LIKE"
            ? likeReasons
            : feedbackLabel === "DISLIKE"
                ? dislikeReasons
                : [];

    return (
        <div className="agent-insights-card">

            {/* ========================= */}
            {/* Header */}
            {/* ========================= */}

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

                {displayScore != null && (

                    <div className="overall-score">

                        <span className="overall-score-number">
                            {displayScore}
                        </span>

                        <span className="overall-score-label">
                            Final Match
                        </span>

                    </div>

                )}

            </div>

            {/* ========================= */}
            {/* Phase 6 Scores */}
            {/* ========================= */}

            <div className="agent-score-grid">

                <ScoreItem
                    label="Skill Fit"
                    score={insights.skillScore}
                    weight="40%"
                />

                <ScoreItem
                    label="Experience Fit"
                    score={insights.experienceScore}
                    weight="25%"
                />

                <ScoreItem
                    label="Role Fit"
                    score={insights.roleFitScore}
                    weight="20%"
                />

                <ScoreItem
                    label="Preference Fit"
                    score={insights.preferenceScore}
                    weight="15%"
                />

            </div>

            {insights.finalScore != null && (

                <section className="agent-section">

                    <h3>
                        Personalized Match
                    </h3>

                    <p>
                        Final Score:{" "}
                        <strong>
                            {insights.finalScore}
                        </strong>
                        {" "} / 100
                    </p>

                    <p
                        style={{
                            opacity: 0.7,
                            fontSize: "14px"
                        }}
                    >
                        Skill 40% · Experience 25% ·
                        Role Fit 20% · Preference 15%
                    </p>

                </section>

            )}

            {/* ========================= */}
            {/* Feedback */}
            {/* ========================= */}

            <section className="agent-section feedback-section">

                <h3>
                    Recommendation Feedback
                </h3>

                <p>
                    Was this recommendation useful?
                </p>

                <div className="feedback-buttons">

                    <button
                        type="button"
                        className={
                            feedbackLabel === "LIKE"
                                ? "feedback-button active"
                                : "feedback-button"
                        }
                        onClick={() => {

                            setFeedbackLabel("LIKE");
                            setFeedbackReason("");
                            setFeedbackMessage("");
                        }}
                    >
                        👍 Like
                    </button>

                    <button
                        type="button"
                        className={
                            feedbackLabel === "DISLIKE"
                                ? "feedback-button active"
                                : "feedback-button"
                        }
                        onClick={() => {

                            setFeedbackLabel("DISLIKE");
                            setFeedbackReason("");
                            setFeedbackMessage("");
                        }}
                    >
                        👎 Dislike
                    </button>

                </div>

                {feedbackLabel && (

                    <div className="feedback-form">

                        <label>
                            Reason
                        </label>

                        <select
                            value={feedbackReason}
                            onChange={(e) =>
                                setFeedbackReason(
                                    e.target.value
                                )
                            }
                        >

                            <option value="">
                                Select a reason
                            </option>

                            {reasonOptions.map(
                                ([value, label]) => (

                                    <option
                                        key={value}
                                        value={value}
                                    >
                                        {label}
                                    </option>

                                )
                            )}

                        </select>

                        <label>
                            Comment
                        </label>

                        <textarea
                            rows="3"
                            placeholder="Optional feedback..."
                            value={feedbackComment}
                            onChange={(e) =>
                                setFeedbackComment(
                                    e.target.value
                                )
                            }
                        />

                        <div className="feedback-actions">

                            <button
                                type="button"
                                className="save-application-button"
                                onClick={saveFeedback}
                                disabled={feedbackSaving}
                            >
                                {
                                    feedbackSaving
                                        ? "Saving..."
                                        : "Save Feedback"
                                }
                            </button>

                            {feedbackMessage && (

                                <span className="profile-message">
                                    {feedbackMessage}
                                </span>

                            )}

                        </div>

                    </div>

                )}

            </section>

            {/* ========================= */}
            {/* Role Analysis */}
            {/* ========================= */}

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

            {/* ========================= */}
            {/* Primary Skills */}
            {/* ========================= */}

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

            {/* ========================= */}
            {/* Strengths */}
            {/* ========================= */}

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

            {/* ========================= */}
            {/* Missing Skills */}
            {/* ========================= */}

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

            {/* ========================= */}
            {/* Application Strategy */}
            {/* ========================= */}

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
                                {insights.applicationAdvice}
                            </p>

                        </>
                    )}

                </section>

            )}

            {/* ========================= */}
            {/* Secondary Skills */}
            {/* ========================= */}

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