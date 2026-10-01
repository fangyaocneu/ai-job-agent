import { useEffect, useRef, useState } from "react";

import ReactMarkdown from "react-markdown";



function parseToolArguments(argumentsText) {

  if (!argumentsText) {
    return {};
  }

  try {
    return JSON.parse(argumentsText);
  } catch {
    return {};
  }
}


function parseResumeEvidence(resultText) {

  if (!resultText) {
    return [];
  }

  const matches = [];

  const pattern =
    /--- Resume Chunk (\d+) ---\s*Resume ID:\s*(\d+)\s*Chunk Index:\s*(\d+)\s*Similarity:\s*([0-9.]+)/g;

  let match;

  while (
    (match = pattern.exec(resultText)) !== null
  ) {

    matches.push({
      rank: Number(match[1]),
      resumeId: Number(match[2]),
      chunkIndex: Number(match[3]),
      similarity: Number(match[4])
    });

  }

  return matches;
}



function Chat() {

  const welcomeMessage = {
    role: "assistant",
    content:
      "Hi, I’m your AI Career Assistant. I can help you evaluate roles, understand your profile, and improve your job-search strategy.",
    toolsUsed: []
  };



  const [messages, setMessages] =
    useState([welcomeMessage]);

  const [conversations, setConversations] =
    useState([]);

  const [
    activeConversationId,
    setActiveConversationId
  ] = useState(null);

  const [input, setInput] =
    useState("");

  const [sending, setSending] =
    useState(false);

  const [uploadingResume, setUploadingResume] =
    useState(false);

  const [resumeUploadMessage, setResumeUploadMessage] =
    useState("");

  const [
    loadingConversations,
    setLoadingConversations
  ] = useState(true);

  const bottomRef =
    useRef(null);



  // =========================
  // Initial Load
  // =========================

  useEffect(() => {
    loadConversations();
  }, []);



  // =========================
  // Auto Scroll
  // =========================

  useEffect(() => {
    bottomRef.current?.scrollIntoView({
      behavior: "smooth"
    });
  }, [messages, sending]);



  // =========================
  // Load Conversation Messages
  // =========================

  const loadConversationMessages =
    async (conversationId) => {

      try {

        const response =
          await fetch(
            `/api/conversations/${conversationId}/messages`
          );

        if (!response.ok) {
          throw new Error(
            `HTTP ${response.status}`
          );
        }

        const data =
          await response.json();

        if (
          Array.isArray(data) &&
          data.length > 0
        ) {

          setMessages(
            data.map(
              (message) => ({
                role: message.role,
                content: message.content,
                toolsUsed:
                  Array.isArray(message.toolsUsed)
                    ? message.toolsUsed
                    : []
              })
            )
          );

        } else {

          setMessages([
            welcomeMessage
          ]);

        }

      } catch (error) {

        console.error(
          "Failed to load conversation messages:",
          error
        );

        setMessages([
          {
            role: "assistant",
            content:
              "I couldn't load this conversation.",
            toolsUsed: []
          }
        ]);

      }

    };



  // =========================
  // Load Conversations
  // =========================

  const loadConversations =
    async (
      preserveActiveConversation = true
    ) => {

      try {

        setLoadingConversations(true);

        const response =
          await fetch(
            "/api/conversations"
          );

        if (!response.ok) {
          throw new Error(
            `HTTP ${response.status}`
          );
        }

        const data =
          await response.json();

        const conversationList =
          Array.isArray(data)
            ? data
            : [];

        setConversations(
          conversationList
        );

        if (
          conversationList.length === 0
        ) {

          if (
            !preserveActiveConversation
          ) {

            setActiveConversationId(
              null
            );

            setMessages([
              welcomeMessage
            ]);

          }

          return;
        }

        if (
          activeConversationId === null
        ) {

          const firstConversationId =
            conversationList[0].id;

          setActiveConversationId(
            firstConversationId
          );

          await loadConversationMessages(
            firstConversationId
          );

        }

      } catch (error) {

        console.error(
          "Failed to load conversations:",
          error
        );

      } finally {

        setLoadingConversations(
          false
        );

      }

    };



  // =========================
  // Create Conversation
  // =========================

  const createConversation =
    async () => {

      try {

        const response =
          await fetch(
            "/api/conversations",
            {
              method: "POST",

              headers: {
                "Content-Type":
                  "application/json"
              },

              body: JSON.stringify({
                title:
                  "New Conversation"
              })
            }
          );

        if (!response.ok) {
          throw new Error(
            `HTTP ${response.status}`
          );
        }

        const conversation =
          await response.json();

        setConversations(
          (current) => [
            conversation,
            ...current
          ]
        );

        setActiveConversationId(
          conversation.id
        );

        setMessages([
          welcomeMessage
        ]);

        setInput("");

      } catch (error) {

        console.error(
          "Failed to create conversation:",
          error
        );

      }

    };



  // =========================
  // Select Conversation
  // =========================

  const selectConversation =
    async (conversationId) => {

      if (
        conversationId ===
        activeConversationId
      ) {
        return;
      }

      setActiveConversationId(
        conversationId
      );

      setInput("");

      await loadConversationMessages(
        conversationId
      );

    };



  // =========================
  // Delete Conversation
  // =========================

  const deleteConversation =
    async (conversationId) => {

      const confirmed =
        window.confirm(
          "Delete this conversation?"
        );

      if (!confirmed) {
        return;
      }

      try {

        const response =
          await fetch(
            `/api/conversations/${conversationId}`,
            {
              method: "DELETE"
            }
          );

        if (!response.ok) {
          throw new Error(
            `HTTP ${response.status}`
          );
        }

        const remaining =
          conversations.filter(
            (conversation) =>
              conversation.id !==
              conversationId
          );

        setConversations(
          remaining
        );

        if (
          conversationId ===
          activeConversationId
        ) {

          if (
            remaining.length > 0
          ) {

            const nextConversation =
              remaining[0];

            setActiveConversationId(
              nextConversation.id
            );

            await loadConversationMessages(
              nextConversation.id
            );

          } else {

            setActiveConversationId(
              null
            );

            setMessages([
              welcomeMessage
            ]);

          }

        }

      } catch (error) {

        console.error(
          "Failed to delete conversation:",
          error
        );

        alert(
          "Could not delete conversation."
        );

      }

    };



  // =========================
  // Send Message
  // =========================

  const sendMessage =
    async () => {

      const trimmed =
        input.trim();

      if (
        !trimmed ||
        sending ||
        activeConversationId === null
      ) {
        return;
      }

      setMessages(
        (current) => [
          ...current,
          {
            role: "user",
            content: trimmed,
            toolsUsed: []
          }
        ]
      );

      setInput("");

      setSending(
        true
      );

      try {

        const response =
          await fetch(
            "/api/chat",
            {
              method: "POST",

              headers: {
                "Content-Type":
                  "application/json"
              },

              body: JSON.stringify({
                conversationId:
                  activeConversationId,

                message:
                  trimmed
              })
            }
          );

        if (!response.ok) {
          throw new Error(
            `HTTP ${response.status}`
          );
        }

        const data =
          await response.json();

        console.log(
          "Chat API response:",
          data
        );

        console.log(
          "Tools used:",
          data.toolsUsed
        );

        setMessages(
          (current) => [
            ...current,
            {
              role:
                "assistant",

              content:
                data.response ||
                "I couldn't generate a response.",

              toolsUsed:
                Array.isArray(
                  data.toolsUsed
                )
                  ? data.toolsUsed
                  : []
            }
          ]
        );

        // Refresh sidebar so the
        // generated title appears immediately.
        await loadConversations(
          true
        );

      } catch (error) {

        console.error(
          "Chat failed:",
          error
        );

        setMessages(
          (current) => [
            ...current,
            {
              role:
                "assistant",

              content:
                "I couldn't reach the AI service. Please try again.",

              toolsUsed: []
            }
          ]
        );

      } finally {

        setSending(
          false
        );

      }

    };



  // =========================
  // Resume Upload
  // =========================

  const uploadResume =
    async (event) => {

      const file =
        event.target.files?.[0];

      if (!file) {
        return;
      }

      if (
        file.type !== "application/pdf" &&
        !file.name
          .toLowerCase()
          .endsWith(".pdf")
      ) {

        setResumeUploadMessage(
          "Please select a PDF file."
        );

        event.target.value = "";

        return;
      }

      try {

        setUploadingResume(
          true
        );

        setResumeUploadMessage(
          `Uploading ${file.name}...`
        );

        const formData =
          new FormData();

        formData.append(
          "file",
          file
        );

        const response =
          await fetch(
            "/api/resume/upload",
            {
              method: "POST",
              body: formData
            }
          );

        if (!response.ok) {

          let errorMessage =
            `HTTP ${response.status}`;

          try {

            const errorText =
              await response.text();

            if (errorText) {
              errorMessage =
                `${errorMessage}: ${errorText}`;
            }

          } catch {
            // Ignore response-body parsing failure.
          }

          throw new Error(
            errorMessage
          );

        }

        const contentType =
          response.headers.get(
            "content-type"
          ) || "";

        let data = null;

        if (
          contentType.includes(
            "application/json"
          )
        ) {

          data =
            await response.json();

        } else {

          data =
            await response.text();

        }

        console.log(
          "Resume upload response:",
          data
        );

        setResumeUploadMessage(
          `Resume uploaded successfully · ${file.name}`
        );

      } catch (error) {

        console.error(
          "Resume upload failed:",
          error
        );

        setResumeUploadMessage(
          "Resume upload failed. Please try again."
        );

      } finally {

        setUploadingResume(
          false
        );

        event.target.value = "";

      }

    };



  // =========================
  // Keyboard
  // =========================

  const handleKeyDown =
    (event) => {

      if (
        event.key === "Enter" &&
        !event.shiftKey
      ) {

        event.preventDefault();

        sendMessage();

      }

    };



  // =========================
  // UI
  // =========================

  return (

    <div className="career-chat-page">

      {/* ========================= */}
      {/* Header */}
      {/* ========================= */}

      <div className="career-chat-header">

        <div className="career-chat-title-row">

          <div className="career-chat-avatar">
            AI
          </div>

          <div>

            <h1>
              AI Career Assistant
            </h1>

            <p>
              Personalized guidance powered by your profile
            </p>

          </div>

        </div>

      </div>



      {/* ========================= */}
      {/* Main Layout */}
      {/* ========================= */}

      <div className="career-chat-layout">

        {/* ========================= */}
        {/* Conversation Sidebar */}
        {/* ========================= */}

        <div className="career-conversation-sidebar">

          <button
            type="button"
            onClick={
              createConversation
            }
            style={{
              width: "100%",
              padding: "10px 12px",
              marginBottom: "14px",
              cursor: "pointer",
              borderRadius: "8px",
              border:
                "1px solid #d1d5db",
              background: "#ffffff",
              fontWeight: "600"
            }}
          >
            + New Chat
          </button>


          {
            loadingConversations && (

              <div
                style={{
                  fontSize: "13px",
                  color: "#6b7280"
                }}
              >
                Loading...
              </div>

            )
          }


          {
            !loadingConversations &&
            conversations.map(
              (conversation) => (

                <div
                  key={
                    conversation.id
                  }
                  style={{
                    display: "flex",
                    alignItems:
                      "center",
                    gap: "6px",
                    marginBottom:
                      "7px"
                  }}
                >

                  <button
                    type="button"

                    onClick={() =>
                      selectConversation(
                        conversation.id
                      )
                    }

                    style={{
                      flex: 1,

                      textAlign:
                        "left",

                      padding:
                        "10px 11px",

                      border:
                        conversation.id ===
                          activeConversationId
                          ? "1px solid #9ca3af"
                          : "1px solid #e5e7eb",

                      borderRadius:
                        "8px",

                      cursor:
                        "pointer",

                      background:
                        conversation.id ===
                          activeConversationId
                          ? "#f3f4f6"
                          : "#ffffff",

                      fontWeight:
                        conversation.id ===
                          activeConversationId
                          ? "600"
                          : "400",

                      overflow:
                        "hidden",

                      textOverflow:
                        "ellipsis",

                      whiteSpace:
                        "nowrap"
                    }}
                  >

                    {
                      conversation.title ||
                      `Conversation ${conversation.id}`
                    }

                  </button>


                  <button
                    type="button"

                    onClick={
                      (event) => {

                        event.stopPropagation();

                        deleteConversation(
                          conversation.id
                        );

                      }
                    }

                    title={
                      "Delete conversation"
                    }

                    style={{
                      border:
                        "none",

                      background:
                        "transparent",

                      cursor:
                        "pointer",

                      fontSize:
                        "15px",

                      opacity:
                        0.65
                    }}
                  >
                    🗑
                  </button>

                </div>

              )
            )
          }

        </div>



        {/* ========================= */}
        {/* Chat */}
        {/* ========================= */}

        <div
          className="career-chat-shell"
          style={{
            flex: 1
          }}
        >

          <div className="career-chat-messages">

            {
              messages.map(
                (
                  message,
                  index
                ) => (

                  <div
                    key={
                      index
                    }
                    className={
                      `career-message-row ${message.role}`
                    }
                  >

                    {
                      message.role ===
                      "assistant" && (

                        <div className="career-message-avatar">
                          AI
                        </div>

                      )
                    }


                    <div
                      className={
                        `career-message-bubble ${message.role}`
                      }
                    >

                      <div className="career-message-name">

                        {
                          message.role ===
                            "user"
                            ? "You"
                            : "Career Assistant"
                        }

                      </div>



                      {/* ========================= */}
                      {/* Message Content */}
                      {/* ========================= */}

                      <div className="career-message-text">

                        {
                          message.role ===
                            "assistant"
                            ? (

                              <ReactMarkdown>
                                {
                                  message.content
                                }
                              </ReactMarkdown>

                            )
                            : (
                              message.content
                            )
                        }

                      </div>



                      {/* ========================= */}
                      {/* Agent Observability */}
                      {/* ========================= */}

                      {
                        message.role ===
                        "assistant" &&

                        Array.isArray(
                          message.toolsUsed
                        ) &&

                        message.toolsUsed.length >
                        0 && (

                          <div className="agent-activity">

                            <div className="agent-activity-title">
                              Agent Activity
                            </div>


                            {
                              message.toolsUsed.map(
                                (
                                  tool,
                                  toolIndex
                                ) => {

                                  const args =
                                    parseToolArguments(
                                      tool.arguments
                                    );

                                  const evidence =
                                    tool.toolName ===
                                      "SearchResume"
                                      ? parseResumeEvidence(
                                        tool.result
                                      )
                                      : [];


                                  return (

                                    <details
                                      key={
                                        `${tool.toolName}-${toolIndex}`
                                      }
                                      className="agent-tool"
                                    >

                                      <summary>
                                        {tool.toolName === "JevRouter"
                                          ? "🧭 Jev Router"
                                          : `⚙ ${tool.toolName}`}
                                      </summary>


                                      <div className="agent-tool-details">
                                        {/* Jev Router */}

                                        {
                                          tool.toolName === "JevRouter" && (

                                            <div
                                              style={{
                                                marginBottom: "12px"
                                              }}
                                            >

                                              <strong>
                                                Intent
                                              </strong>

                                              <div
                                                style={{
                                                  marginTop: "5px"
                                                }}
                                              >
                                                {
                                                  String(tool.result || "")
                                                    .replace(/^Intent:\s*/i, "")
                                                }
                                              </div>

                                            </div>

                                          )
                                        }

                                        {/* Query */}

                                        {
                                          args.query && (

                                            <div
                                              style={{
                                                marginBottom:
                                                  "12px"
                                              }}
                                            >

                                              <strong>
                                                Query
                                              </strong>

                                              <div
                                                style={{
                                                  marginTop:
                                                    "5px"
                                                }}
                                              >
                                                {
                                                  args.query
                                                }
                                              </div>

                                            </div>

                                          )
                                        }


                                        {/* Limit */}

                                        {
                                          args.limit && (

                                            <div
                                              style={{
                                                marginBottom:
                                                  "12px"
                                              }}
                                            >

                                              <strong>
                                                Requested Results
                                              </strong>

                                              <div>
                                                {
                                                  args.limit
                                                }
                                              </div>

                                            </div>

                                          )
                                        }


                                        {/* RAG Evidence */}

                                        {
                                          evidence.length >
                                          0 && (

                                            <div>

                                              <strong>
                                                Retrieved Evidence
                                              </strong>

                                              <div
                                                style={{
                                                  marginTop:
                                                    "7px",

                                                  display:
                                                    "flex",

                                                  flexDirection:
                                                    "column",

                                                  gap:
                                                    "6px"
                                                }}
                                              >

                                                {
                                                  evidence.map(
                                                    (item) => (

                                                      <div
                                                        key={
                                                          `${item.resumeId}-${item.chunkIndex}`
                                                        }
                                                        style={{
                                                          display:
                                                            "flex",

                                                          alignItems:
                                                            "center",

                                                          justifyContent:
                                                            "space-between",

                                                          gap:
                                                            "12px",

                                                          padding:
                                                            "8px 10px",

                                                          borderRadius:
                                                            "7px",

                                                          background:
                                                            "#f3f4f6"
                                                        }}
                                                      >

                                                        <div>

                                                          <strong>
                                                            #{item.rank}
                                                          </strong>

                                                          {" "}

                                                          Resume {
                                                            item.resumeId
                                                          }

                                                          {" · "}

                                                          Chunk {
                                                            item.chunkIndex
                                                          }

                                                        </div>


                                                        <div
                                                          style={{
                                                            fontWeight:
                                                              "600",

                                                            fontVariantNumeric:
                                                              "tabular-nums"
                                                          }}
                                                        >
                                                          Similarity{" "}
                                                          {
                                                            item.similarity
                                                              .toFixed(
                                                                3
                                                              )
                                                          }
                                                        </div>

                                                      </div>

                                                    )
                                                  )
                                                }

                                              </div>

                                            </div>

                                          )
                                        }


                                        {/* Other tools */}

                                        {
                                          tool.toolName !== "JevRouter" &&
                                          !args.query &&
                                          evidence.length === 0 && (

                                            <div>

                                              <strong>
                                                Arguments
                                              </strong>

                                              <div
                                                style={{
                                                  marginTop:
                                                    "5px"
                                                }}
                                              >
                                                {
                                                  tool.arguments ||
                                                  "No arguments"
                                                }
                                              </div>

                                            </div>

                                          )
                                        }

                                      </div>

                                    </details>

                                  );

                                }
                              )
                            }

                          </div>

                        )
                      }

                    </div>

                  </div>

                )
              )
            }



            {/* ========================= */}
            {/* Typing */}
            {/* ========================= */}

            {
              sending && (

                <div className="career-message-row assistant">

                  <div className="career-message-avatar">
                    AI
                  </div>

                  <div className="career-message-bubble assistant">

                    <div className="career-message-name">
                      Career Assistant
                    </div>

                    <div className="career-typing">
                      <span />
                      <span />
                      <span />
                    </div>

                  </div>

                </div>

              )
            }


            <div
              ref={
                bottomRef
              }
            />

          </div>



          {/* ========================= */}
          {/* Composer */}
          {/* ========================= */}

          <div className="career-chat-composer">

            <div className="career-chat-input-wrapper">


              {/* Resume Upload */}

              <label
                title="Upload resume PDF"
                aria-label="Upload resume PDF"

                style={{
                  width: "38px",
                  height: "38px",

                  flexShrink: 0,

                  display: "grid",
                  placeItems: "center",

                  borderRadius: "10px",

                  background:
                    "#f1f5f9",

                  color:
                    "#475569",

                  cursor:
                    uploadingResume
                      ? "wait"
                      : "pointer",

                  fontSize:
                    "18px",

                  userSelect:
                    "none",

                  opacity:
                    uploadingResume
                      ? 0.65
                      : 1
                }}
              >

                <input
                  type="file"

                  accept="
                    application/pdf,.pdf
                  "

                  onChange={
                    uploadResume
                  }

                  disabled={
                    uploadingResume
                  }

                  style={{
                    display: "none"
                  }}
                />

                {
                  uploadingResume
                    ? "…"
                    : "📎"
                }

              </label>



              {/* Chat Input */}

              <textarea
                value={
                  input
                }

                onChange={
                  (event) =>
                    setInput(
                      event.target.value
                    )
                }

                onKeyDown={
                  handleKeyDown
                }

                placeholder={
                  activeConversationId
                    ? "Ask about roles, skills, applications, or your career..."
                    : "Create or select a conversation first..."
                }

                disabled={
                  activeConversationId ===
                  null
                }

                rows="1"
              />



              {/* Send */}

              <button
                type="button"

                onClick={
                  sendMessage
                }

                disabled={
                  sending ||
                  !input.trim() ||
                  activeConversationId ===
                  null
                }

                className="career-chat-send"
              >
                ↑
              </button>

            </div>



            {/* Resume Upload Status */}

            {
              resumeUploadMessage && (

                <div
                  role="status"

                  style={{
                    maxWidth:
                      "820px",

                    margin:
                      "8px auto 0",

                    padding:
                      "8px 10px",

                    borderRadius:
                      "9px",

                    background:
                      resumeUploadMessage
                        .toLowerCase()
                        .includes(
                          "failed"
                        ) ||
                        resumeUploadMessage
                          .toLowerCase()
                          .includes(
                            "please select"
                          )
                        ? "#fff1f2"
                        : "#f0fdf4",

                    color:
                      resumeUploadMessage
                        .toLowerCase()
                        .includes(
                          "failed"
                        ) ||
                        resumeUploadMessage
                          .toLowerCase()
                          .includes(
                            "please select"
                          )
                        ? "#be123c"
                        : "#166534",

                    textAlign:
                      "center",

                    fontSize:
                      "12px",

                    fontWeight:
                      "600"
                  }}
                >

                  {
                    resumeUploadMessage
                  }

                </div>

              )
            }



            <div className="career-chat-hint">

              {
                activeConversationId
                  ? `Conversation ${activeConversationId} · Enter to send · Shift + Enter for new line`
                  : "Select a conversation to begin"
              }

            </div>

          </div>

        </div>

      </div>

    </div>

  );

}



export default Chat;