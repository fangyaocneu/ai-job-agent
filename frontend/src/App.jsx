import { useEffect, useMemo, useState } from "react";
import "./App.css";

function App() {
  const [stats, setStats] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [followUpsDue, setFollowUpsDue] = useState([]);

  const [search, setSearch] = useState("");
  const [minScore, setMinScore] = useState(70);
  const [sortOrder, setSortOrder] = useState("desc");

  // Phase 4
  const [activeView, setActiveView] = useState("ALL");
  const [selectedJob, setSelectedJob] = useState(null);

  const [applicationStage, setApplicationStage] =
    useState("NOT_APPLIED");

  const [notes, setNotes] = useState("");
  const [appliedAt, setAppliedAt] = useState("");
  const [followUpDate, setFollowUpDate] = useState("");

  const [detailLoading, setDetailLoading] = useState(false);
  const [savingApplication, setSavingApplication] =
    useState(false);

  const loadFollowUpsDue = async () => {
    try {
      const response = await fetch(
        "/api/jobs/follow-ups-due"
      );

      if (!response.ok) {
        throw new Error("Failed to load follow-ups");
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

  useEffect(() => {
    fetch("/api/jobs/stats")
      .then((response) => response.json())
      .then((data) => setStats(data))
      .catch((error) => {
        console.error("Failed to load stats:", error);
      });

    fetch("/api/jobs/high-match")
      .then((response) => response.json())
      .then((data) => setJobs(data))
      .catch((error) => {
        console.error("Failed to load jobs:", error);
      });

    loadFollowUpsDue();
  }, []);

  const updateStatus = async (id, status, value) => {
    try {
      const response = await fetch(
        `/api/jobs/${id}/status`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            status,
            value,
          }),
        }
      );

      const success = await response.json();

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

        setSelectedJob((currentJob) => {
          if (!currentJob || currentJob.id !== id) {
            return currentJob;
          }

          return {
            ...currentJob,
            [status]: value,
          };
        });
      }
    } catch (error) {
      console.error(
        "Failed to update status:",
        error
      );
    }
  };

  const openJobDetails = async (id) => {
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

      const job = await response.json();

      setSelectedJob(job);

      setApplicationStage(
        job.applicationStage || "NOT_APPLIED"
      );

      setNotes(job.notes || "");
      setAppliedAt(job.appliedAt || "");
      setFollowUpDate(job.followUpDate || "");
    } catch (error) {
      console.error(
        "Failed to load job details:",
        error
      );
    } finally {
      setDetailLoading(false);
    }
  };

  const saveApplicationDetails = async () => {
    if (!selectedJob) {
      return;
    }

    try {
      setSavingApplication(true);

      const response = await fetch(
        `/api/jobs/${selectedJob.id}/application`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            applicationStage,
            notes,
            appliedAt,
            followUpDate,
          }),
        }
      );

      const success = await response.json();

      if (success) {
        setSelectedJob((currentJob) => ({
          ...currentJob,
          applicationStage,
          notes,
          appliedAt,
          followUpDate,
        }));

        setJobs((currentJobs) =>
          currentJobs.map((job) =>
            job.id === selectedJob.id
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

        alert("Application details saved.");
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
    setApplicationStage("NOT_APPLIED");
    setNotes("");
    setAppliedAt("");
    setFollowUpDate("");
  };

  const getFollowUpStatus = (dateString) => {
    if (!dateString) {
      return null;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const followUp = new Date(
      `${dateString}T00:00:00`
    );
    followUp.setHours(0, 0, 0, 0);

    const millisecondsPerDay =
      1000 * 60 * 60 * 24;

    const diffDays = Math.round(
      (followUp.getTime() - today.getTime()) /
        millisecondsPerDay
    );

    if (diffDays < 0) {
      const overdueDays = Math.abs(diffDays);

      return {
        type: "overdue",
        label: `Overdue by ${overdueDays} day${
          overdueDays === 1 ? "" : "s"
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
      label: `Due in ${diffDays} day${
        diffDays === 1 ? "" : "s"
      }`,
    };
  };

  const matchesView = (job) => {
    switch (activeView) {
      case "FAVORITES":
        return job.favorite === true;

      case "APPLIED":
        return (
          job.applicationStage === "APPLIED"
        );

      case "FOLLOW_UP":
        return followUpsDue.some(
          (dueJob) => dueJob.id === job.id
        );

      case "INTERVIEWING":
        return [
          "OA",
          "PHONE_SCREEN",
          "INTERVIEW",
          "FINAL_ROUND",
        ].includes(job.applicationStage);

      case "OFFER":
        return (
          job.applicationStage === "OFFER"
        );

      case "REJECTED":
        return (
          job.applicationStage === "REJECTED"
        );

      case "ALL":
      default:
        return true;
    }
  };

  const filteredJobs = useMemo(() => {
    return jobs
      .filter((job) => {
        const keyword = search.toLowerCase();

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
          job.matchScore >= minScore;

        const notIgnored = !job.ignored;

        const matchesCurrentView =
          matchesView(job);

        return (
          matchesSearch &&
          matchesScore &&
          notIgnored &&
          matchesCurrentView
        );
      })
      .sort((a, b) => {
        if (sortOrder === "desc") {
          return b.matchScore - a.matchScore;
        }

        return a.matchScore - b.matchScore;
      });
  }, [
    jobs,
    search,
    minScore,
    sortOrder,
    activeView,
    followUpsDue,
  ]);

  if (!stats) {
    return (
      <div className="dashboard">
        <h2>Loading...</h2>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <h1>AI Job Agent Dashboard</h1>

      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Jobs</h3>
          <p>{stats.totalJobs}</p>
        </div>

        <div className="stat-card">
          <h3>High Match Jobs</h3>
          <p>{stats.highMatchJobs}</p>
        </div>

        <div className="stat-card">
          <h3>Unscored Jobs</h3>
          <p>{stats.unscoredJobs}</p>
        </div>

        <div className="stat-card">
          <h3>Average Score</h3>
          <p>{stats.averageScore}</p>
        </div>

        <div className="stat-card">
          <h3>Follow-ups Due</h3>
          <p>{followUpsDue.length}</p>
        </div>
      </div>

      <div className="view-tabs">
        <button
          className={
            activeView === "ALL"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("ALL")
          }
        >
          All
        </button>

        <button
          className={
            activeView === "FAVORITES"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("FAVORITES")
          }
        >
          Favorites
        </button>

        <button
          className={
            activeView === "APPLIED"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("APPLIED")
          }
        >
          Applied
        </button>

        <button
          className={
            activeView === "FOLLOW_UP"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("FOLLOW_UP")
          }
        >
          Follow-up Due ({followUpsDue.length})
        </button>

        <button
          className={
            activeView === "INTERVIEWING"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("INTERVIEWING")
          }
        >
          Interviewing
        </button>

        <button
          className={
            activeView === "OFFER"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("OFFER")
          }
        >
          Offers
        </button>

        <button
          className={
            activeView === "REJECTED"
              ? "active"
              : ""
          }
          onClick={() =>
            setActiveView("REJECTED")
          }
        >
          Rejected
        </button>
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="Search title, company, location..."
          value={search}
          onChange={(e) =>
            setSearch(e.target.value)
          }
        />

        <select
          value={minScore}
          onChange={(e) =>
            setMinScore(
              Number(e.target.value)
            )
          }
        >
          <option value={70}>
            Score ≥ 70
          </option>

          <option value={75}>
            Score ≥ 75
          </option>

          <option value={80}>
            Score ≥ 80
          </option>

          <option value={85}>
            Score ≥ 85
          </option>
        </select>

        <select
          value={sortOrder}
          onChange={(e) =>
            setSortOrder(e.target.value)
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

      {detailLoading && (
        <div className="job-detail-card">
          <h2>
            Loading job details...
          </h2>
        </div>
      )}

      {selectedJob &&
        !detailLoading && (
          <div className="job-detail-card">
            <div className="job-detail-header">
              <div>
                <h2>
                  {selectedJob.title}
                </h2>

                <h3>
                  {selectedJob.company ||
                    "Unknown Company"}
                </h3>
              </div>

              <button
                className="close-detail-button"
                onClick={
                  closeJobDetails
                }
              >
                Close
              </button>
            </div>

            <p>
              <strong>
                Location:
              </strong>{" "}
              {selectedJob.location ||
                "Not specified"}
            </p>

            <p>
              <strong>
                Match Score:
              </strong>{" "}
              {selectedJob.matchScore ??
                "Not scored"}
            </p>

            <p>
              <strong>
                Published:
              </strong>{" "}
              {selectedJob.publishedAt
                ? new Date(
                    selectedJob.publishedAt
                  ).toLocaleDateString()
                : "Not specified"}
            </p>

            {selectedJob.followUpDate &&
              (() => {
                const status =
                  getFollowUpStatus(
                    selectedJob.followUpDate
                  );

                return (
                  <div
                    className={`follow-up-badge ${status.type}`}
                  >
                    {status.label}
                  </div>
                );
              })()}

            <div className="detail-section">
              <h3>
                AI Match Analysis
              </h3>

              <p>
                <strong>
                  Reason:
                </strong>{" "}
                {selectedJob.matchReason ||
                  "Not available"}
              </p>

              <p>
                <strong>
                  Skill Gap:
                </strong>{" "}
                {selectedJob.matchGap ||
                  "Not available"}
              </p>
            </div>

            <div className="detail-section">
              <h3>
                Job Description
              </h3>

              <p className="job-description">
                {selectedJob.description ||
                  "No description available."}
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
                value={followUpDate}
                onChange={(e) =>
                  setFollowUpDate(
                    e.target.value
                  )
                }
              />

              <label>Notes</label>

              <textarea
                rows="6"
                placeholder="Add application notes..."
                value={notes}
                onChange={(e) =>
                  setNotes(
                    e.target.value
                  )
                }
              />

              <button
                className="save-application-button"
                onClick={
                  saveApplicationDetails
                }
                disabled={
                  savingApplication
                }
              >
                {savingApplication
                  ? "Saving..."
                  : "Save Application"}
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
                {selectedJob.favorite
                  ? "★ Favorited"
                  : "☆ Favorite"}
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
                {selectedJob.applied
                  ? "Applied ✓"
                  : "Mark Applied"}
              </button>
            </div>

            <a
              href={selectedJob.url}
              target="_blank"
              rel="noreferrer"
            >
              Open Original Job Posting
            </a>
          </div>
        )}

      <h2>
        {activeView === "ALL"
          ? "High Match Jobs"
          : activeView ===
            "FAVORITES"
          ? "Favorite Jobs"
          : activeView ===
            "APPLIED"
          ? "Applied Jobs"
          : activeView ===
            "FOLLOW_UP"
          ? "Follow-ups Due"
          : activeView ===
            "INTERVIEWING"
          ? "Interviewing"
          : activeView === "OFFER"
          ? "Offers"
          : "Rejected Jobs"}
      </h2>

      <div className="jobs-grid">
        {filteredJobs.length === 0 ? (
          <div className="job-card">
            <p>
              No jobs match this view.
            </p>
          </div>
        ) : (
          filteredJobs.map(
            (job) => {
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
                    {job.title} @{" "}
                    {job.company}
                  </h3>

                  <p className="score">
                    Match Score:{" "}
                    {job.matchScore}
                  </p>

                  <p>
                    <strong>
                      Location:
                    </strong>{" "}
                    {job.location ||
                      "Not specified"}
                  </p>

                  <p>
                    <strong>
                      Application:
                    </strong>{" "}
                    {job.applicationStage ||
                      "NOT_APPLIED"}
                  </p>

                  {followUpStatus && (
                    <div
                      className={`follow-up-badge ${followUpStatus.type}`}
                    >
                      {followUpStatus.label}
                    </div>
                  )}

                  {job.appliedAt && (
                    <p>
                      <strong>
                        Applied:
                      </strong>{" "}
                      {job.appliedAt}
                    </p>
                  )}

                  {job.followUpDate && (
                    <p>
                      <strong>
                        Follow-up:
                      </strong>{" "}
                      {job.followUpDate}
                    </p>
                  )}

                  <p>
                    <strong>
                      Reason:
                    </strong>{" "}
                    {job.matchReason ||
                      "Not available"}
                  </p>

                  <p>
                    <strong>
                      Gap:
                    </strong>{" "}
                    {job.matchGap ||
                      "Not available"}
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
                      {job.favorite
                        ? "★ Favorited"
                        : "☆ Favorite"}
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
                      {job.applied
                        ? "Applied ✓"
                        : "Mark Applied"}
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
                      {job.ignored
                        ? "Ignored ✓"
                        : "Ignore"}
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
            }
          )
        )}
      </div>
    </div>
  );
}

export default App;