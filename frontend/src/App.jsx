import { useEffect, useMemo, useState } from "react";
import "./App.css";
import AgentInsights from "./components/AgentInsights";
import Profile from "./components/Profile";
import Chat from "./components/Chat";

function App() {
  // =========================
  // Main data
  // =========================

  const [stats, setStats] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [followUpsDue, setFollowUpsDue] = useState([]);

  const [agentRuns, setAgentRuns] = useState([]);
  const [agentRunsLoading, setAgentRunsLoading] = useState(false);

  // =========================
  // Filters / navigation
  // =========================

  const [search, setSearch] = useState("");
  const [minScore, setMinScore] = useState(70);
  const [sortOrder, setSortOrder] = useState("desc");

  const [page, setPage] = useState("DASHBOARD");

  // =========================
  // Job detail
  // =========================

  const [selectedJob, setSelectedJob] = useState(null);

  const [applicationStage, setApplicationStage] =
    useState("NOT_APPLIED");

  const [notes, setNotes] = useState("");
  const [appliedAt, setAppliedAt] = useState("");
  const [followUpDate, setFollowUpDate] = useState("");

  const [detailLoading, setDetailLoading] = useState(false);

  const [savingApplication, setSavingApplication] =
    useState(false);

  // =========================
  // API loading
  // =========================

  const loadFollowUpsDue = async () => {
    try {
      const response = await fetch(
        "/api/jobs/follow-ups-due"
      );

      if (!response.ok) {
        throw new Error(
          "Failed to load follow-ups"
        );
      }

      const data = await response.json();
      setFollowUpsDue(data);

    } catch (error) {
      console.error(
        "Failed to load follow-ups:",
        error
      );
    }
  };

  const loadStats = async () => {
    try {
      const response = await fetch(
        "/api/jobs/stats"
      );

      if (!response.ok) {
        throw new Error(
          "Failed to load stats"
        );
      }

      const data = await response.json();
      setStats(data);

    } catch (error) {
      console.error(
        "Failed to load stats:",
        error
      );
    }
  };

  const loadJobs = async () => {
    try {
      const response = await fetch(
        "/api/jobs/high-match"
      );

      if (!response.ok) {
        throw new Error(
          "Failed to load jobs"
        );
      }

      const data = await response.json();
      setJobs(data);

    } catch (error) {
      console.error(
        "Failed to load jobs:",
        error
      );
    }
  };

  const loadAgentRuns = async () => {
    try {
      setAgentRunsLoading(true);

      const response = await fetch(
        "/api/agents/runs"
      );

      if (!response.ok) {
        throw new Error(
          "Failed to load agent runs"
        );
      }

      const data = await response.json();
      setAgentRuns(data);

    } catch (error) {
      console.error(
        "Failed to load agent runs:",
        error
      );

    } finally {
      setAgentRunsLoading(false);
    }
  };

  useEffect(() => {
    loadStats();
    loadJobs();
    loadFollowUpsDue();
    loadAgentRuns();
  }, []);

  // =========================
  // Job actions
  // =========================

  const updateStatus = async (
    id,
    status,
    value
  ) => {
    try {
      const response = await fetch(
        `/api/jobs/${id}/status`,
        {
          method: "PATCH",
          headers: {
            "Content-Type":
              "application/json",
          },
          body: JSON.stringify({
            status,
            value,
          }),
        }
      );

      const success =
        await response.json();

      if (success) {
        setJobs((currentJobs) =>
          currentJobs.map((job) =>
            job.id === id
              ? {
                ...job,
                [status]: value,
              }
              : job
          )
        );

        setSelectedJob(
          (currentJob) => {
            if (
              !currentJob ||
              currentJob.id !== id
            ) {
              return currentJob;
            }

            return {
              ...currentJob,
              [status]: value,
            };
          }
        );
      }

    } catch (error) {
      console.error(
        "Failed to update status:",
        error
      );
    }
  };

  const openJobDetails = async (
    id
  ) => {
    try {
      setDetailLoading(true);

      const response = await fetch(
        `/api/jobs/${id}`
      );

      if (!response.ok) {
        throw new Error(
          "Failed to load job details"
        );
      }

      const job =
        await response.json();

      setSelectedJob(job);

      setApplicationStage(
        job.applicationStage ||
        "NOT_APPLIED"
      );

      setNotes(
        job.notes || ""
      );

      setAppliedAt(
        job.appliedAt || ""
      );

      setFollowUpDate(
        job.followUpDate || ""
      );

    } catch (error) {
      console.error(
        "Failed to load job details:",
        error
      );

    } finally {
      setDetailLoading(false);
    }
  };

  const saveApplicationDetails =
    async () => {

      if (!selectedJob) {
        return;
      }

      try {
        setSavingApplication(true);

        const response =
          await fetch(
            `/api/jobs/${selectedJob.id}/application`,
            {
              method: "PATCH",
              headers: {
                "Content-Type":
                  "application/json",
              },
              body: JSON.stringify({
                applicationStage,
                notes,
                appliedAt,
                followUpDate,
              }),
            }
          );

        const success =
          await response.json();

        if (success) {
          setSelectedJob(
            (currentJob) => ({
              ...currentJob,
              applicationStage,
              notes,
              appliedAt,
              followUpDate,
            })
          );

          setJobs(
            (currentJobs) =>
              currentJobs.map(
                (job) =>
                  job.id ===
                    selectedJob.id
                    ? {
                      ...job,
                      applicationStage,
                      notes,
                      appliedAt,
                      followUpDate,
                    }
                    : job
              )
          );

          await loadFollowUpsDue();

          alert(
            "Application details saved."
          );

        } else {
          alert(
            "Failed to save application details."
          );
        }

      } catch (error) {
        console.error(
          "Failed to save application details:",
          error
        );

        alert(
          "Failed to save application details."
        );

      } finally {
        setSavingApplication(false);
      }
    };

  const closeJobDetails = () => {
    setSelectedJob(null);

    setApplicationStage(
      "NOT_APPLIED"
    );

    setNotes("");
    setAppliedAt("");
    setFollowUpDate("");
  };

  // =========================
  // Helpers
  // =========================

  const getFollowUpStatus = (
    dateString
  ) => {
    if (!dateString) {
      return null;
    }

    const today =
      new Date();

    today.setHours(
      0,
      0,
      0,
      0
    );

    const followUp =
      new Date(
        `${dateString}T00:00:00`
      );

    followUp.setHours(
      0,
      0,
      0,
      0
    );

    const millisecondsPerDay =
      1000 * 60 * 60 * 24;

    const diffDays =
      Math.round(
        (
          followUp.getTime() -
          today.getTime()
        ) /
        millisecondsPerDay
      );

    if (diffDays < 0) {
      const overdueDays =
        Math.abs(diffDays);

      return {
        type: "overdue",
        label:
          `Overdue by ${overdueDays} day${overdueDays === 1
            ? ""
            : "s"
          }`,
      };
    }

    if (diffDays === 0) {
      return {
        type: "today",
        label: "Due Today",
      };
    }

    return {
      type: "upcoming",
      label:
        `Due in ${diffDays} day${diffDays === 1
          ? ""
          : "s"
        }`,
    };
  };

  const formatDuration = (
    durationMs
  ) => {
    if (
      durationMs === null ||
      durationMs === undefined
    ) {
      return "-";
    }

    if (durationMs < 1000) {
      return `${durationMs} ms`;
    }

    return `${(
      durationMs / 1000
    ).toFixed(2)} s`;
  };

  const formatDateTime = (
    value
  ) => {
    if (!value) {
      return "-";
    }

    return new Date(
      value
    ).toLocaleString();
  };

  // =========================
  // Filtering
  // =========================

  const filteredJobs =
    useMemo(() => {

      return jobs
        .filter((job) => {

          const keyword =
            search.toLowerCase();

          const matchesSearch =
            job.title
              ?.toLowerCase()
              .includes(keyword) ||
            job.company
              ?.toLowerCase()
              .includes(keyword) ||
            job.location
              ?.toLowerCase()
              .includes(keyword);

          const matchesScore =
            job.matchScore != null &&
            job.matchScore >=
            minScore;

          const notIgnored =
            !job.ignored;

          return (
            matchesSearch &&
            matchesScore &&
            notIgnored
          );
        })
        .sort((a, b) => {

          if (
            sortOrder === "desc"
          ) {
            return (
              b.matchScore -
              a.matchScore
            );
          }

          return (
            a.matchScore -
            b.matchScore
          );
        });

    }, [
      jobs,
      search,
      minScore,
      sortOrder,
    ]);

  const appliedJobs =
    jobs.filter(
      (job) =>
        job.applicationStage ===
        "APPLIED"
    );

  const interviewingJobs =
    jobs.filter(
      (job) =>
        [
          "OA",
          "PHONE_SCREEN",
          "INTERVIEW",
          "FINAL_ROUND",
        ].includes(
          job.applicationStage
        )
    );

  const offerJobs =
    jobs.filter(
      (job) =>
        job.applicationStage ===
        "OFFER"
    );

  const rejectedJobs =
    jobs.filter(
      (job) =>
        job.applicationStage ===
        "REJECTED"
    );

  // =========================
  // Agent metrics
  // =========================

  const successfulRuns =
    agentRuns.filter(
      (run) =>
        run.status === "SUCCESS"
    );

  const failedRuns =
    agentRuns.filter(
      (run) =>
        run.status === "FAILED"
    );

  const runningRuns =
    agentRuns.filter(
      (run) =>
        run.status === "RUNNING"
    );

  const durationRuns =
    agentRuns.filter(
      (run) =>
        run.durationMs != null
    );

  const averageAgentDuration =
    durationRuns.length > 0
      ? Math.round(
        durationRuns.reduce(
          (sum, run) =>
            sum +
            Number(
              run.durationMs
            ),
          0
        ) /
        durationRuns.length
      )
      : 0;

  const successRate =
    agentRuns.length > 0
      ? Math.round(
        (
          successfulRuns.length /
          agentRuns.length
        ) *
        100
      )
      : 0;

  // =========================
  // Reusable Job Card
  // =========================

  const renderJobCard = (
    job
  ) => {

    const followUpStatus =
      getFollowUpStatus(
        job.followUpDate
      );

    return (
      <div
        className="job-card"
        key={job.id}
      >
        <h3>
          {job.title}
          {" @ "}
          {job.company}
        </h3>

        <p className="score">
          Match Score:{" "}
          {
            job.matchScore ??
            "Not scored"
          }
        </p>

        <p>
          <strong>
            Location:
          </strong>{" "}
          {
            job.location ||
            "Not specified"
          }
        </p>

        <p>
          <strong>
            Application:
          </strong>{" "}
          {
            job.applicationStage ||
            "NOT_APPLIED"
          }
        </p>

        {
          followUpStatus &&
          (
            <div
              className={
                `follow-up-badge ${followUpStatus.type}`
              }
            >
              {
                followUpStatus.label
              }
            </div>
          )
        }

        {
          job.appliedAt &&
          (
            <p>
              <strong>
                Applied:
              </strong>{" "}
              {
                job.appliedAt
              }
            </p>
          )
        }

        {
          job.followUpDate &&
          (
            <p>
              <strong>
                Follow-up:
              </strong>{" "}
              {
                job.followUpDate
              }
            </p>
          )
        }

        <p>
          <strong>
            Reason:
          </strong>{" "}
          {
            job.matchReason ||
            "Not available"
          }
        </p>

        <p>
          <strong>
            Gap:
          </strong>{" "}
          {
            job.matchGap ||
            "Not available"
          }
        </p>

        <div className="job-actions">

          <button
            onClick={() =>
              updateStatus(
                job.id,
                "favorite",
                !job.favorite
              )
            }
          >
            {
              job.favorite
                ? "★ Favorited"
                : "☆ Favorite"
            }
          </button>

          <button
            onClick={() =>
              updateStatus(
                job.id,
                "applied",
                !job.applied
              )
            }
          >
            {
              job.applied
                ? "Applied ✓"
                : "Mark Applied"
            }
          </button>

          <button
            onClick={() =>
              updateStatus(
                job.id,
                "ignored",
                !job.ignored
              )
            }
          >
            {
              job.ignored
                ? "Ignored ✓"
                : "Ignore"
            }
          </button>

          <button
            onClick={() =>
              openJobDetails(
                job.id
              )
            }
          >
            View Details
          </button>

        </div>

        <a
          href={job.url}
          target="_blank"
          rel="noreferrer"
        >
          View Job
        </a>

      </div>
    );
  };

  // =========================
  // Job Detail
  // =========================

  const renderJobDetails = () => {

    if (detailLoading) {
      return (
        <div className="job-detail-card">
          <h2>
            Loading job details...
          </h2>
        </div>
      );
    }

    if (!selectedJob) {
      return null;
    }

    return (
      <div className="job-detail-card">

        <div className="job-detail-header">

          <div>
            <h2>
              {
                selectedJob.title
              }
            </h2>

            <h3>
              {
                selectedJob.company ||
                "Unknown Company"
              }
            </h3>
          </div>

          <button
            className="close-detail-button"
            onClick={closeJobDetails}
          >
            Close
          </button>

        </div>

        <p>
          <strong>
            Location:
          </strong>{" "}
          {
            selectedJob.location ||
            "Not specified"
          }
        </p>

        <p>
          <strong>
            Match Score:
          </strong>{" "}
          {
            selectedJob.matchScore ??
            "Not scored"
          }
        </p>

        <p>
          <strong>
            Published:
          </strong>{" "}
          {
            selectedJob.publishedAt
              ? new Date(
                selectedJob.publishedAt
              ).toLocaleDateString()
              : "Not specified"
          }
        </p>

        {
          selectedJob.followUpDate &&
          (() => {

            const status =
              getFollowUpStatus(
                selectedJob.followUpDate
              );

            return (
              <div
                className={
                  `follow-up-badge ${status.type}`
                }
              >
                {
                  status.label
                }
              </div>
            );
          })()
        }

        <div className="detail-section">

          <h3>
            AI Match Analysis
          </h3>

          <p>
            <strong>
              Reason:
            </strong>{" "}
            {
              selectedJob.matchReason ||
              "Not available"
            }
          </p>

          <p>
            <strong>
              Skill Gap:
            </strong>{" "}
            {
              selectedJob.matchGap ||
              "Not available"
            }
          </p>

        </div>

        <AgentInsights
          jobId={
            selectedJob.id
          }
        />

        <div className="detail-section">

          <h3>
            Job Description
          </h3>

          <p className="job-description">
            {
              selectedJob.description ||
              "No description available."
            }
          </p>

        </div>

        <div className="detail-section">

          <h3>
            Application Tracking
          </h3>

          <label>
            Application Stage
          </label>

          <select
            value={
              applicationStage
            }
            onChange={(e) =>
              setApplicationStage(
                e.target.value
              )
            }
          >
            <option value="NOT_APPLIED">
              Not Applied
            </option>

            <option value="APPLIED">
              Applied
            </option>

            <option value="OA">
              Online Assessment
            </option>

            <option value="PHONE_SCREEN">
              Phone Screen
            </option>

            <option value="INTERVIEW">
              Interview
            </option>

            <option value="FINAL_ROUND">
              Final Round
            </option>

            <option value="OFFER">
              Offer
            </option>

            <option value="REJECTED">
              Rejected
            </option>
          </select>

          <label>
            Applied Date
          </label>

          <input
            type="date"
            value={appliedAt}
            onChange={(e) =>
              setAppliedAt(
                e.target.value
              )
            }
          />

          <label>
            Follow-up Date
          </label>

          <input
            type="date"
            value={
              followUpDate
            }
            onChange={(e) =>
              setFollowUpDate(
                e.target.value
              )
            }
          />

          <label>
            Notes
          </label>

          <textarea
            rows="6"
            placeholder=
            "Add application notes..."
            value={notes}
            onChange={(e) =>
              setNotes(
                e.target.value
              )
            }
          />

          <button
            className=
            "save-application-button"
            onClick=
            {saveApplicationDetails}
            disabled={
              savingApplication
            }
          >
            {
              savingApplication
                ? "Saving..."
                : "Save Application"
            }
          </button>

        </div>

        <div className="job-actions">

          <button
            onClick={() =>
              updateStatus(
                selectedJob.id,
                "favorite",
                !selectedJob.favorite
              )
            }
          >
            {
              selectedJob.favorite
                ? "★ Favorited"
                : "☆ Favorite"
            }
          </button>

          <button
            onClick={() =>
              updateStatus(
                selectedJob.id,
                "applied",
                !selectedJob.applied
              )
            }
          >
            {
              selectedJob.applied
                ? "Applied ✓"
                : "Mark Applied"
            }
          </button>

        </div>

        <a
          href={
            selectedJob.url
          }
          target="_blank"
          rel="noreferrer"
        >
          Open Original Job Posting
        </a>

      </div>
    );
  };

  // =========================
  // Dashboard
  // =========================

  const renderDashboard = () => (
    <>
      <div className="page-header">
        <div>
          <h1>
            Dashboard
          </h1>

          <p>
            Overview of your AI-powered job search.
          </p>
        </div>
      </div>

      <div className="stats-grid">

        <div className="stat-card">
          <h3>
            Total Jobs
          </h3>

          <p>
            {
              stats?.totalJobs ??
              0
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            High Match Jobs
          </h3>

          <p>
            {
              stats?.highMatchJobs ??
              0
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Unscored Jobs
          </h3>

          <p>
            {
              stats?.unscoredJobs ??
              0
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Average Score
          </h3>

          <p>
            {
              stats?.averageScore ??
              0
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Follow-ups Due
          </h3>

          <p>
            {
              followUpsDue.length
            }
          </p>
        </div>

      </div>

      {renderJobDetails()}

      <div className="dashboard-section">

        <div className="section-heading">

          <div>
            <h2>
              Top Matches
            </h2>

            <p>
              Highest-scoring jobs currently in your pipeline.
            </p>
          </div>

          <button
            className="secondary-button"
            onClick={() =>
              setPage("JOBS")
            }
          >
            View All Jobs
          </button>

        </div>

        <div className="jobs-grid">

          {
            filteredJobs
              .slice(0, 4)
              .map(
                renderJobCard
              )
          }

        </div>

      </div>
    </>
  );

  // =========================
  // Jobs
  // =========================

  const renderJobs = () => (
    <>
      <div className="page-header">

        <div>
          <h1>
            Jobs
          </h1>

          <p>
            Search and review AI-scored opportunities.
          </p>
        </div>

      </div>

      <div className="filters">

        <input
          type="text"
          placeholder=
          "Search title, company, location..."
          value={search}
          onChange={(e) =>
            setSearch(
              e.target.value
            )
          }
        />

        <select
          value={minScore}
          onChange={(e) =>
            setMinScore(
              Number(
                e.target.value
              )
            )
          }
        >
          <option value={0}>
            All Scores
          </option>

          <option value={40}>
            Score ≥ 40
          </option>

          <option value={50}>
            Score ≥ 50
          </option>

          <option value={60}>
            Score ≥ 60
          </option>

          <option value={70}>
            Score ≥ 70
          </option>

          <option value={80}>
            Score ≥ 80
          </option>
        </select>

        <select
          value={sortOrder}
          onChange={(e) =>
            setSortOrder(
              e.target.value
            )
          }
        >
          <option value="desc">
            Highest Score First
          </option>

          <option value="asc">
            Lowest Score First
          </option>
        </select>

      </div>

      {renderJobDetails()}

      <div className="jobs-grid">

        {
          filteredJobs.length === 0
            ? (
              <div className="job-card">
                <p>
                  No jobs match your filters.
                </p>
              </div>
            )
            : filteredJobs.map(
              renderJobCard
            )
        }

      </div>
    </>
  );

  // =========================
  // Pipeline
  // =========================

  const renderPipelineColumn = (
    title,
    columnJobs
  ) => (
    <div className="pipeline-column">

      <div className="pipeline-column-header">

        <h3>
          {title}
        </h3>

        <span>
          {
            columnJobs.length
          }
        </span>

      </div>

      <div className="pipeline-list">

        {
          columnJobs.length === 0
            ? (
              <p className="empty-state">
                No jobs
              </p>
            )
            : columnJobs.map(
              (job) => (
                <div
                  className="pipeline-card"
                  key={job.id}
                  onClick={() =>
                    openJobDetails(
                      job.id
                    )
                  }
                >
                  <strong>
                    {
                      job.title
                    }
                  </strong>

                  <span>
                    {
                      job.company
                    }
                  </span>

                  <span>
                    Score:{" "}
                    {
                      job.matchScore ??
                      "-"
                    }
                  </span>
                </div>
              )
            )
        }

      </div>

    </div>
  );

  const renderPipeline = () => (
    <>
      <div className="page-header">

        <div>
          <h1>
            Pipeline
          </h1>

          <p>
            Track application progress from applied to offer.
          </p>
        </div>

      </div>

      {renderJobDetails()}

      <div className="pipeline-board">

        {
          renderPipelineColumn(
            "Applied",
            appliedJobs
          )
        }

        {
          renderPipelineColumn(
            "Interviewing",
            interviewingJobs
          )
        }

        {
          renderPipelineColumn(
            "Offers",
            offerJobs
          )
        }

        {
          renderPipelineColumn(
            "Rejected",
            rejectedJobs
          )
        }

      </div>
    </>
  );

  // =========================
  // Follow-ups
  // =========================

  const renderFollowUps = () => (
    <>
      <div className="page-header">

        <div>
          <h1>
            Follow-ups
          </h1>

          <p>
            Applications that need your attention.
          </p>
        </div>

      </div>

      {renderJobDetails()}

      <div className="jobs-grid">

        {
          followUpsDue.length === 0
            ? (
              <div className="job-card">
                <p>
                  No follow-ups due.
                </p>
              </div>
            )
            : followUpsDue.map(
              renderJobCard
            )
        }

      </div>
    </>
  );

  // =========================
  // Agents
  // =========================

  const renderAgents = () => (
    <>
      <div className="page-header">

        <div>
          <h1>
            Agents
          </h1>

          <p>
            Monitor your Phase 5 multi-agent execution pipeline.
          </p>
        </div>

        <button
          className="secondary-button"
          onClick={
            loadAgentRuns
          }
          disabled={
            agentRunsLoading
          }
        >
          {
            agentRunsLoading
              ? "Refreshing..."
              : "Refresh"
          }
        </button>

      </div>

      <div className="stats-grid">

        <div className="stat-card">
          <h3>
            Total Runs
          </h3>

          <p>
            {
              agentRuns.length
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Successful
          </h3>

          <p>
            {
              successfulRuns.length
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Failed
          </h3>

          <p>
            {
              failedRuns.length
            }
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Success Rate
          </h3>

          <p>
            {
              successRate
            }%
          </p>
        </div>

        <div className="stat-card">
          <h3>
            Avg Duration
          </h3>

          <p>
            {
              formatDuration(
                averageAgentDuration
              )
            }
          </p>
        </div>

      </div>

      <div className="agents-grid">

        <div className="agent-overview-card">

          <div className="agent-icon">
            1
          </div>

          <div>
            <h3>
              PreFilter Agent
            </h3>

            <p>
              Removes irrelevant roles, unsupported locations,
              and seniority mismatches before expensive AI calls.
            </p>
          </div>

        </div>

        <div className="agent-overview-card">

          <div className="agent-icon">
            2
          </div>

          <div>
            <h3>
              Job Analysis Agent
            </h3>

            <p>
              Extracts role type, seniority, primary skills,
              secondary skills, and experience requirements.
            </p>
          </div>

        </div>

        <div className="agent-overview-card">

          <div className="agent-icon">
            3
          </div>

          <div>
            <h3>
              Match Agent
            </h3>

            <p>
              Scores skill match, experience fit,
              role fit, strengths, and missing skills.
            </p>
          </div>

        </div>

        <div className="agent-overview-card">

          <div className="agent-icon">
            4
          </div>

          <div>
            <h3>
              Strategy Agent
            </h3>

            <p>
              Generates recommendation, priority,
              resume focus, concerns, and application advice.
            </p>
          </div>

        </div>

      </div>

      <div className="agent-system-card">

        <h2>
          Agent Pipeline
        </h2>

        <div className="agent-flow">

          <div>
            Job Sources
          </div>

          <span>
            →
          </span>

          <div>
            PreFilter
          </div>

          <span>
            →
          </span>

          <div>
            Job Analysis
          </div>

          <span>
            →
          </span>

          <div>
            Match
          </div>

          <span>
            →
          </span>

          <div>
            Strategy
          </div>

          <span>
            →
          </span>

          <div>
            Database
          </div>

        </div>

      </div>

      {
        runningRuns.length > 0 &&
        (
          <div className="agent-system-card">

            <h2>
              Currently Running
            </h2>

            <div className="agent-run-table-wrapper">

              <table className="agent-run-table">

                <thead>
                  <tr>
                    <th>
                      Agent
                    </th>

                    <th>
                      Job
                    </th>

                    <th>
                      Status
                    </th>

                    <th>
                      Started
                    </th>
                  </tr>
                </thead>

                <tbody>

                  {
                    runningRuns.map(
                      (run) => (
                        <tr
                          key={
                            run.id
                          }
                        >
                          <td>
                            {
                              run.agentName
                            }
                          </td>

                          <td>
                            #
                            {
                              run.jobId
                            }
                          </td>

                          <td>
                            <span className="agent-status running">
                              RUNNING
                            </span>
                          </td>

                          <td>
                            {
                              formatDateTime(
                                run.startedAt
                              )
                            }
                          </td>
                        </tr>
                      )
                    )
                  }

                </tbody>

              </table>

            </div>

          </div>
        )
      }

      <div className="agent-system-card">

        <div className="section-heading">

          <div>
            <h2>
              Recent Agent Activity
            </h2>

            <p>
              Latest execution records from agent_runs.
            </p>
          </div>

        </div>

        {
          agentRunsLoading
            ? (
              <p>
                Loading agent activity...
              </p>
            )
            : agentRuns.length === 0
              ? (
                <p>
                  No agent execution history found.
                </p>
              )
              : (
                <div className="agent-run-table-wrapper">

                  <table className="agent-run-table">

                    <thead>
                      <tr>

                        <th>
                          ID
                        </th>

                        <th>
                          Agent
                        </th>

                        <th>
                          Job
                        </th>

                        <th>
                          Status
                        </th>

                        <th>
                          Duration
                        </th>

                        <th>
                          Started
                        </th>

                      </tr>
                    </thead>

                    <tbody>

                      {
                        agentRuns
                          .slice(
                            0,
                            30
                          )
                          .map(
                            (run) => (

                              <tr
                                key={
                                  run.id
                                }
                              >

                                <td>
                                  #
                                  {
                                    run.id
                                  }
                                </td>

                                <td>
                                  <strong>
                                    {
                                      run.agentName
                                    }
                                  </strong>
                                </td>

                                <td>
                                  {
                                    run.jobId
                                  }
                                </td>

                                <td>

                                  <span
                                    className={
                                      `agent-status ${run.status?.toLowerCase()}`
                                    }
                                  >
                                    {
                                      run.status
                                    }
                                  </span>

                                </td>

                                <td>
                                  {
                                    formatDuration(
                                      run.durationMs
                                    )
                                  }
                                </td>

                                <td>
                                  {
                                    formatDateTime(
                                      run.startedAt
                                    )
                                  }
                                </td>

                              </tr>

                            )
                          )
                      }

                    </tbody>

                  </table>

                </div>
              )
        }

      </div>

      <div className="agent-system-card">

        <div className="section-heading">

          <div>
            <h2>
              Failures
            </h2>

            <p>
              Recent failed agent executions and error messages.
            </p>
          </div>

        </div>

        {
          failedRuns.length === 0
            ? (
              <p className="agent-success-message">
                No recent agent failures.
              </p>
            )
            : (
              <div className="agent-failure-list">

                {
                  failedRuns
                    .slice(
                      0,
                      10
                    )
                    .map(
                      (run) => (

                        <div
                          className="agent-failure-card"
                          key={
                            run.id
                          }
                        >

                          <div>

                            <strong>
                              {
                                run.agentName
                              }
                            </strong>

                            <span>
                              Job #
                              {
                                run.jobId
                              }
                            </span>

                          </div>

                          <p>
                            {
                              run.errorMessage ||
                              "Unknown error"
                            }
                          </p>

                        </div>

                      )
                    )
                }

              </div>
            )
        }

      </div>
    </>
  );

  // =========================
  // Settings
  // =========================

  const renderSettings = () => (
    <>
      <div className="page-header">

        <div>
          <h1>
            Settings
          </h1>

          <p>
            Configure your job agent preferences.
          </p>
        </div>

      </div>

      <div className="settings-card">

        <h3>
          Candidate Profile
        </h3>

        <p>
          Java / Backend / Software Engineer
        </p>

      </div>

      <div className="settings-card">

        <h3>
          AI Match Threshold
        </h3>

        <p>
          Current strategy threshold:
          <strong>
            {" "}60
          </strong>
        </p>

      </div>

      <div className="settings-card">

        <h3>
          Job Sources
        </h3>

        <p>
          Remotive
        </p>

        <p>
          RemoteOK
        </p>

        <p>
          Greenhouse
        </p>

        <p>
          Lever
        </p>

      </div>
    </>
  );

  // =========================
  // Page switch
  // =========================

 const renderPage = () => {

  switch (page) {

    case "JOBS":
      return renderJobs();

    case "PIPELINE":
      return renderPipeline();

    case "FOLLOW_UPS":
      return renderFollowUps();

    case "AGENTS":
      return renderAgents();

    case "CHAT":
      return <Chat />;

    case "PROFILE":
      return <Profile />;

    case "SETTINGS":
      return renderSettings();

    case "DASHBOARD":
    default:
      return renderDashboard();
  }
};

// =========================
// Loading
// =========================

if (!stats) {
  return (
    <div className="app-loading">
      Loading AI Job Agent...
    </div>
  );
}

// =========================
// Layout
// =========================

return (
  <div className="app-shell">

    <aside className="sidebar">

      <div className="sidebar-brand">

        <div className="brand-icon">
          AI
        </div>

        <div>

          <strong>
            Job Agent
          </strong>

          <span>
            Phase 6
          </span>

        </div>

      </div>

      <nav className="sidebar-nav">

        <button
          className={
            page === "DASHBOARD"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("DASHBOARD")
          }
        >
          <span>
            ◫
          </span>

          Dashboard
        </button>

        <button
          className={
            page === "JOBS"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("JOBS")
          }
        >
          <span>
            ⌕
          </span>

          Jobs
        </button>

        <button
          className={
            page === "PIPELINE"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("PIPELINE")
          }
        >
          <span>
            ▤
          </span>

          Pipeline
        </button>

        <button
          className={
            page === "FOLLOW_UPS"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("FOLLOW_UPS")
          }
        >
          <span>
            ◷
          </span>

          Follow-ups

          {
            followUpsDue.length > 0 && (
              <span className="sidebar-count">
                {followUpsDue.length}
              </span>
            )
          }

        </button>

        <button
          className={
            page === "AGENTS"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("AGENTS")
          }
        >
          <span>
            ◈
          </span>

          Agents
        </button>

        <button
          className={
            page === "CHAT"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("CHAT")
          }
        >
          <span>
            ◉
          </span>

          Chat
        </button>

        <button
          className={
            page === "PROFILE"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("PROFILE")
          }
        >
          <span>
            ♙
          </span>

          Profile
        </button>

        <button
          className={
            page === "SETTINGS"
              ? "active"
              : ""
          }
          onClick={() =>
            setPage("SETTINGS")
          }
        >
          <span>
            ⚙
          </span>

          Settings
        </button>

      </nav>

      <div className="sidebar-footer">

        <span className="status-dot" />

        Agent online

      </div>

    </aside>

    <main className="main-content">
      {renderPage()}
    </main>

  </div>
);
}

export default App;