import { useEffect, useMemo, useState } from "react";
import "./App.css";

function App() {
  const [stats, setStats] = useState(null);
  const [jobs, setJobs] = useState([]);

  const [search, setSearch] = useState("");
  const [minScore, setMinScore] = useState(70);
  const [sortOrder, setSortOrder] = useState("desc");

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
              ? { ...job, [status]: value }
              : job
          )
        );
      }
    } catch (error) {
      console.error("Failed to update status:", error);
    }
  };

  const filteredJobs = useMemo(() => {
    return jobs
      .filter((job) => {
        const keyword = search.toLowerCase();

        const matchesSearch =
          job.title?.toLowerCase().includes(keyword) ||
          job.company?.toLowerCase().includes(keyword) ||
          job.location?.toLowerCase().includes(keyword);

        const matchesScore =
          job.matchScore != null &&
          job.matchScore >= minScore;

        const notIgnored = !job.ignored;

        return matchesSearch && matchesScore && notIgnored;
      })
      .sort((a, b) => {
        if (sortOrder === "desc") {
          return b.matchScore - a.matchScore;
        }

        return a.matchScore - b.matchScore;
      });
  }, [jobs, search, minScore, sortOrder]);

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
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="Search title, company, location..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          value={minScore}
          onChange={(e) =>
            setMinScore(Number(e.target.value))
          }
        >
          <option value={70}>Score ≥ 70</option>
          <option value={75}>Score ≥ 75</option>
          <option value={80}>Score ≥ 80</option>
          <option value={85}>Score ≥ 85</option>
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

      <h2>High Match Jobs</h2>

      <div className="jobs-grid">
        {filteredJobs.length === 0 ? (
          <div className="job-card">
            <p>No jobs match your filters.</p>
          </div>
        ) : (
          filteredJobs.map((job) => (
            <div
              className="job-card"
              key={job.id}
            >
              <h3>
                {job.title} @ {job.company}
              </h3>

              <p className="score">
                Match Score: {job.matchScore}
              </p>

              <p>
                <strong>Location:</strong>{" "}
                {job.location ||
                  "Not specified"}
              </p>

              <p>
                <strong>Reason:</strong>{" "}
                {job.matchReason ||
                  "Not available"}
              </p>

              <p>
                <strong>Gap:</strong>{" "}
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
              </div>

              <a
                href={job.url}
                target="_blank"
                rel="noreferrer"
              >
                View Job
              </a>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default App;