import { useEffect, useState } from "react";

function Profile() {

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const targetRoleOptions = [
    "Software Engineer",
    "Backend Engineer",
    "Java Developer",
    "Full Stack Engineer"
  ];

  const locationOptions = [
    "United States",
    "California",
    "Remote"
  ];

  const workModeOptions = [
    "Remote",
    "Hybrid",
    "Onsite"
  ];

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {

    try {

      setLoading(true);

      const response =
        await fetch("/api/profile");

      if (!response.ok) {
        throw new Error(
          "Failed to load candidate profile"
        );
      }

      const data =
        await response.json();

      setProfile(data);

    } catch (error) {

      console.error(
        "Failed to load profile:",
        error
      );

      setMessage(
        "Failed to load profile."
      );

    } finally {

      setLoading(false);
    }
  };

  const updateTextField = (
    field,
    value
  ) => {

    setProfile(
      (current) => ({
        ...current,
        [field]: value
      })
    );
  };

  const updateListField = (
    field,
    value
  ) => {

    const values =
      value
        .split(",")
        .map((item) => item.trim());

    setProfile(
      (current) => ({
        ...current,
        [field]: values
      })
    );
  };

  const toggleListOption = (
    field,
    value
  ) => {

    setProfile(
      (current) => {

        const currentValues =
          current[field] || [];

        const exists =
          currentValues.includes(value);

        const nextValues =
          exists
            ? currentValues.filter(
                (item) => item !== value
              )
            : [
                ...currentValues,
                value
              ];

        return {
          ...current,
          [field]: nextValues
        };
      }
    );
  };

  const listToText = (values) => {

    if (!values) {
      return "";
    }

    return values.join(", ");
  };

  const cleanList = (values) => {

    if (!values) {
      return [];
    }

    return values
      .map((item) => item.trim())
      .filter(Boolean);
  };

  const saveProfile = async () => {

    try {

      setSaving(true);
      setMessage("");

      const cleanedProfile = {
        ...profile,

        languages:
          cleanList(
            profile.languages
          ),

        backendTechnologies:
          cleanList(
            profile.backendTechnologies
          ),

        cloudTechnologies:
          cleanList(
            profile.cloudTechnologies
          ),

        databases:
          cleanList(
            profile.databases
          ),

        frontendTechnologies:
          cleanList(
            profile.frontendTechnologies
          ),

        tools:
          cleanList(
            profile.tools
          ),

        targetRoles:
          cleanList(
            profile.targetRoles
          ),

        experienceHighlights:
          cleanList(
            profile.experienceHighlights
          ),

        projectHighlights:
          cleanList(
            profile.projectHighlights
          ),

        preferredLocations:
          cleanList(
            profile.preferredLocations
          ),

        preferredWorkModes:
          cleanList(
            profile.preferredWorkModes
          )
      };

      const response =
        await fetch(
          "/api/profile",
          {
            method: "PUT",

            headers: {
              "Content-Type":
                "application/json"
            },

            body:
              JSON.stringify(
                cleanedProfile
              )
          }
        );

      if (!response.ok) {

        throw new Error(
          "Failed to save candidate profile"
        );
      }

      const updated =
        await response.json();

      setProfile(updated);

      setMessage(
        "Profile saved successfully."
      );

    } catch (error) {

      console.error(
        "Failed to save profile:",
        error
      );

      setMessage(
        "Failed to save profile."
      );

    } finally {

      setSaving(false);
    }
  };

  if (loading) {

    return (
      <div className="settings-card">
        Loading candidate profile...
      </div>
    );
  }

  if (!profile) {

    return (
      <div className="settings-card">
        Candidate profile not found.
      </div>
    );
  }

  return (
    <>

      <div className="page-header">

        <div>

          <h1>
            Candidate Profile
          </h1>

          <p>
            Configure the profile used by your
            AI agents to evaluate jobs.
          </p>

        </div>

      </div>

      <div className="profile-card">

        <div className="profile-field">

          <label>
            Name
          </label>

          <input
            type="text"
            value={profile.name || ""}
            onChange={(e) =>
              updateTextField(
                "name",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Languages
          </label>

          <input
            type="text"
            value={
              listToText(
                profile.languages
              )
            }
            onChange={(e) =>
              updateListField(
                "languages",
                e.target.value
              )
            }
          />

          <small>
            Separate values with commas.
          </small>

        </div>

        <div className="profile-field">

          <label>
            Backend Technologies
          </label>

          <input
            type="text"
            value={
              listToText(
                profile.backendTechnologies
              )
            }
            onChange={(e) =>
              updateListField(
                "backendTechnologies",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Cloud Technologies
          </label>

          <input
            type="text"
            value={
              listToText(
                profile.cloudTechnologies
              )
            }
            onChange={(e) =>
              updateListField(
                "cloudTechnologies",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Databases
          </label>

          <input
            type="text"
            value={
              listToText(
                profile.databases
              )
            }
            onChange={(e) =>
              updateListField(
                "databases",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Frontend Technologies
          </label>

          <input
            type="text"
            value={
              listToText(
                profile.frontendTechnologies
              )
            }
            onChange={(e) =>
              updateListField(
                "frontendTechnologies",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Tools
          </label>

          <input
            type="text"
            value={
              listToText(
                profile.tools
              )
            }
            onChange={(e) =>
              updateListField(
                "tools",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Target Roles
          </label>

          <div className="profile-options">

            {targetRoleOptions.map(
              (option) => {

                const active =
                  profile.targetRoles?.includes(
                    option
                  );

                return (
                  <button
                    key={option}
                    type="button"
                    className={
                      active
                        ? "profile-option active"
                        : "profile-option"
                    }
                    onClick={() =>
                      toggleListOption(
                        "targetRoles",
                        option
                      )
                    }
                  >
                    {option}
                  </button>
                );
              }
            )}

          </div>

        </div>

        <div className="profile-field">

          <label>
            Preferred Locations
          </label>

          <div className="profile-options">

            {locationOptions.map(
              (option) => {

                const active =
                  profile.preferredLocations?.includes(
                    option
                  );

                return (
                  <button
                    key={option}
                    type="button"
                    className={
                      active
                        ? "profile-option active"
                        : "profile-option"
                    }
                    onClick={() =>
                      toggleListOption(
                        "preferredLocations",
                        option
                      )
                    }
                  >
                    {option}
                  </button>
                );
              }
            )}

          </div>

        </div>

        <div className="profile-field">

          <label>
            Preferred Work Modes
          </label>

          <div className="profile-options">

            {workModeOptions.map(
              (option) => {

                const active =
                  profile.preferredWorkModes?.includes(
                    option
                  );

                return (
                  <button
                    key={option}
                    type="button"
                    className={
                      active
                        ? "profile-option active"
                        : "profile-option"
                    }
                    onClick={() =>
                      toggleListOption(
                        "preferredWorkModes",
                        option
                      )
                    }
                  >
                    {option}
                  </button>
                );
              }
            )}

          </div>

        </div>

        <div className="profile-field">

          <label>
            Experience Highlights
          </label>

          <textarea
            rows="6"
            value={
              listToText(
                profile.experienceHighlights
              )
            }
            onChange={(e) =>
              updateListField(
                "experienceHighlights",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-field">

          <label>
            Project Highlights
          </label>

          <textarea
            rows="6"
            value={
              listToText(
                profile.projectHighlights
              )
            }
            onChange={(e) =>
              updateListField(
                "projectHighlights",
                e.target.value
              )
            }
          />

        </div>

        <div className="profile-actions">

          <button
            className="save-application-button"
            onClick={saveProfile}
            disabled={saving}
          >

            {
              saving
                ? "Saving..."
                : "Save Profile"
            }

          </button>

          {
            message && (
              <span className="profile-message">
                {message}
              </span>
            )
          }

        </div>

      </div>

    </>
  );
}

export default Profile;