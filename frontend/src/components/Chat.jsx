import { useEffect, useRef, useState } from "react";

function Chat() {

  const welcomeMessage = {
    role: "assistant",
    content:
      "Hi, I’m your AI Career Assistant. I can help you evaluate roles, understand your profile, and improve your job-search strategy."
  };

  const [messages, setMessages] =
    useState([
      welcomeMessage
    ]);

  const [conversations, setConversations] =
    useState([]);

  const [activeConversationId, setActiveConversationId] =
    useState(null);

  const [input, setInput] =
    useState("");

  const [sending, setSending] =
    useState(false);

  const [loadingConversations, setLoadingConversations] =
    useState(true);

  const bottomRef =
    useRef(null);

  // =========================
  // Load Conversations
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
  // Get Conversations
  // =========================
  const deleteConversation = async (
    conversationId
  ) => {

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
            conversation.id !== conversationId
        );

      setConversations(
        remaining
      );

      if (
        conversationId ===
        activeConversationId
      ) {

        if (remaining.length > 0) {

          const nextConversation =
            remaining[0];

          setActiveConversationId(
            nextConversation.id
          );

          try {

            const messageResponse =
              await fetch(
                `/api/conversations/${nextConversation.id}/messages`
              );

            if (
              messageResponse.ok
            ) {

              const messageData =
                await messageResponse.json();

              if (
                messageData.length > 0
              ) {

                setMessages(
                  messageData.map(
                    (message) => ({
                      role:
                        message.role,

                      content:
                        message.content
                    })
                  )
                );

              } else {

                setMessages([
                  welcomeMessage
                ]);
              }
            }

          } catch (error) {

            console.error(
              "Failed to load next conversation:",
              error
            );

            setMessages([
              welcomeMessage
            ]);
          }

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

  const loadConversations = async () => {

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

      setConversations(
        data
      );

      if (
        data.length > 0 &&
        activeConversationId === null
      ) {

        const firstConversationId =
          data[0].id;

        setActiveConversationId(
          firstConversationId
        );

        try {

          const messageResponse =
            await fetch(
              `/api/conversations/${firstConversationId}/messages`
            );

          if (
            messageResponse.ok
          ) {

            const messageData =
              await messageResponse.json();

            if (
              messageData.length > 0
            ) {

              setMessages(
                messageData.map(
                  (message) => ({
                    role:
                      message.role,

                    content:
                      message.content
                  })
                )
              );
            }
          }

        } catch (error) {

          console.error(
            "Failed to load initial conversation:",
            error
          );
        }
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

  const createConversation = async () => {

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
              title: "New Conversation"
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
  // Switch Conversation
  // =========================

  const selectConversation = async (
    conversationId
  ) => {

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
        data.length === 0
      ) {

        setMessages([
          welcomeMessage
        ]);

        return;
      }

      setMessages(
        data.map(
          (message) => ({
            role: message.role,
            content: message.content
          })
        )
      );

    } catch (error) {

      console.error(
        "Failed to load conversation messages:",
        error
      );

      setMessages([
        {
          role: "assistant",
          content:
            "I couldn't load this conversation."
        }
      ]);
    }
  };

  // =========================
  // Send Message
  // =========================

  const sendMessage = async () => {

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
          content: trimmed
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

      setMessages(
        (current) => [
          ...current,
          {
            role: "assistant",

            content:
              data.response ||
              "I couldn't generate a response."
          }
        ]
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
            role: "assistant",

            content:
              "I couldn't reach the AI service. Please try again."
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
  // Keyboard
  // =========================

  const handleKeyDown = (
    event
  ) => {

    if (
      event.key === "Enter" &&
      !event.shiftKey
    ) {

      event.preventDefault();

      sendMessage();
    }
  };

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

      <div
        style={{
          display: "flex",
          height: "calc(100vh - 150px)",
          gap: "16px"
        }}
      >

        {/* ========================= */}
        {/* Conversation Sidebar */}
        {/* ========================= */}

        <div
          style={{
            width: "260px",
            borderRight:
              "1px solid #e5e7eb",
            padding: "12px",
            overflowY: "auto"
          }}
        >

          <button
            type="button"
            onClick={
              createConversation
            }
            style={{
              width: "100%",
              padding: "10px",
              marginBottom: "14px",
              cursor: "pointer"
            }}
          >
            + New Chat
          </button>

          {loadingConversations && (

            <div>
              Loading...
            </div>

          )}

          {!loadingConversations &&
            conversations.map(
              (conversation) => (

                <div
                  key={conversation.id}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "6px",
                    marginBottom: "6px"
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
                      textAlign: "left",
                      padding: "10px",
                      border:
                        "1px solid #e5e7eb",
                      borderRadius: "8px",
                      cursor: "pointer",
                      fontWeight:
                        conversation.id ===
                          activeConversationId
                          ? "600"
                          : "400"
                    }}
                  >

                    {
                      conversation.title ||
                      `Conversation ${conversation.id}`
                    }

                  </button>

                  <button
                    type="button"
                    onClick={(event) => {

                      event.stopPropagation();

                      deleteConversation(
                        conversation.id
                      );
                    }}
                    title="Delete conversation"
                    style={{
                      border: "none",
                      background: "transparent",
                      cursor: "pointer",
                      fontSize: "16px"
                    }}
                  >
                    🗑
                  </button>

                </div>

              )
            )}

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

            {messages.map(
              (
                message,
                index
              ) => (

                <div
                  key={index}
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

                    <div className="career-message-text">
                      {
                        message.content
                      }
                    </div>

                  </div>

                </div>

              )
            )}

            {sending && (

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

            )}

            <div ref={bottomRef} />

          </div>

          {/* ========================= */}
          {/* Composer */}
          {/* ========================= */}

          <div className="career-chat-composer">

            <div className="career-chat-input-wrapper">

              <textarea
                value={input}
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

            <div className="career-chat-hint">

              {activeConversationId
                ? `Conversation ${activeConversationId} · Press Enter to send · Shift + Enter for a new line`
                : "Select a conversation to begin"}

            </div>

          </div>

        </div>

      </div>

    </div>
  );
}

export default Chat;