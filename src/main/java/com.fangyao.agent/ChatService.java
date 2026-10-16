package com.fangyao.agent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.fangyao.agent.skills.AgentTask;
import com.fangyao.agent.skills.ChatRequestInputFactory;
import com.fangyao.agent.skills.SkillRegistry;
import com.fangyao.agent.skills.SkillRuntime;
import com.fangyao.agent.skills.SkillSelector;
import com.fangyao.agent.skills.TaskSelector;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseStreamEvent;

@Service

public class ChatService {

    private final OpenAIClient client;
    private final CandidateProfileRepository profileRepository;
    private final ChatToolService chatToolService;
    private final ChatMessageRepository chatMessageRepository;
    private final ResumeRagService resumeRagService;
    private final ResumeGapService resumeGapService;
    private final ConversationTitleService conversationTitleService;
    private final ChatConversationRepository conversationRepository;
    private final JevIntentRouter jevIntentRouter;
    private final UrlContentService urlContentService;
    private final JobDescriptionService jobDescriptionService;
    private final SkillSelector skillSelector;

    // =========================
    // Constructor
    // =========================
    public ChatService() {

        this.client
                = OpenAIOkHttpClient.fromEnv();

        this.profileRepository
                = new CandidateProfileRepository();

        this.chatToolService
                = new ChatToolService();

        this.chatMessageRepository
                = new ChatMessageRepository();

        this.resumeRagService
                = new ResumeRagService();

        this.resumeGapService
                = new ResumeGapService();

        this.conversationTitleService
                = new ConversationTitleService();

        this.conversationRepository
                = new ChatConversationRepository();
        this.jevIntentRouter
                = new JevIntentRouter();
        try {
            SkillRegistry skillRegistry
                    = new SkillRegistry(Path.of("skills"));

            this.skillSelector
                    = new SkillSelector(skillRegistry);

            System.out.println(
                    "[SKILLS] Registry initialized: "
                    + skillRegistry.size()
                    + " mapped skills"
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to initialize Agent Skills",
                    e
            );
        }
        this.urlContentService
                = new UrlContentService();
        this.jobDescriptionService
                = new JobDescriptionService();
    }

    // =========================
    // Tool 1: Get Top Matches
    // =========================
    @JsonClassDescription(
            """
            Gets the candidate's current top job matches
            from the application's real job database.

            Use this tool when the user asks which jobs
            they should apply to, their best opportunities,
            strongest matches, recommended jobs,
            or what they should focus on applying to.
            """
    )
    public static class GetTopMatches {

        @JsonPropertyDescription(
                """
                Maximum number of job matches requested by the user.
                If the user does not specify a number, use 5.
                """
        )
        public Integer limit;

        public GetTopMatches() {
        }
    }

    // =========================
    // Tool 2: Get Job Insights
    // =========================
    @JsonClassDescription(
            """
            Gets detailed match information for one specific job
            from the application's real job database.

            Use this tool when the user asks why a specific job
            received a certain match score, asks about the fit
            of a specific job, or wants to understand skill fit,
            experience fit, role fit, preference fit, strengths,
            weaknesses, or skill gaps for a specific job ID.
            """
    )
    public static class GetJobInsights {

        @JsonPropertyDescription(
                """
                The numeric job ID of the job the user is asking about.
                """
        )
        public Long jobId;

        public GetJobInsights() {
        }
    }

    // =========================
    // Tool 3: Get Follow-Ups
    // =========================
    @JsonClassDescription(
            """
            Gets applications that currently have follow-ups due
            from the application's real database.

            Use this tool when the user asks which applications
            need attention, whether any follow-ups are due or overdue,
            what they should follow up on next,
            or similar application follow-up questions.
            """
    )
    public static class GetFollowUps {

        @JsonPropertyDescription(
                """
        Scope of follow-ups requested by the user.

        Allowed values:

        due
        - follow-ups due today or overdue

        overdue
        - follow-ups with a date before today

        today
        - follow-ups scheduled exactly for today

        all
        - all applications that have a follow-up date

        Choose the value that most closely matches the user's wording.
        If the user does not specify a scope, use "due".
        """
        )
        public String scope;

        public GetFollowUps() {
        }
    }

    // =========================
    // Tool 4: Get Applications
    // =========================
    @JsonClassDescription(
            """
            Gets the candidate's currently tracked job applications
            from the application's real database.

            Use this tool when the user asks what jobs they have applied to,
            which applications they are tracking,
            their current application pipeline,
            application stages,
            or the status of their applications.
            """
    )
    public static class GetApplications {

        @JsonPropertyDescription(
                """
                Optional application stage requested by the user.

                Examples:
                APPLIED
                OA
                PHONE_SCREEN
                INTERVIEW
                FINAL_ROUND
                OFFER
                REJECTED

                If no specific stage is requested,
                use "ALL".
                """
        )
        public String stage;

        public GetApplications() {
        }
    }

    // =========================
    // Tool 5: Candidate Profile
    // =========================
    @JsonClassDescription(
            """
            Gets the candidate's current stored profile
            from the application's database.

            Use this tool when the user asks about their skills,
            target roles, preferred locations, work preferences,
            experience, projects, technologies,
            or asks to summarize their candidate profile.
            """
    )
    public static class GetCandidateProfile {

        @JsonPropertyDescription(
                """
                Optional section of the profile requested by the user.
                Examples: skills, roles, locations, experience,
                projects, technologies, or all.
                """
        )
        public String section;

        public GetCandidateProfile() {
        }
    }
    // =========================
// Tool 6: Search Resume
// =========================

    @JsonClassDescription(
            """
        Searches the candidate's uploaded resume using semantic retrieval.

        Use this tool when the user asks about resume-specific
        experience, projects, technologies, achievements,
        education, or evidence from their resume.

        This tool retrieves only the most relevant resume sections
        instead of returning the full resume.
        """
    )
    public static class SearchResume {

        @JsonPropertyDescription(
                """
            Natural-language search query describing the resume
            information needed.

            Examples:
            backend experience
            AWS cloud experience
            Java projects
            testing achievements
            education
            """
        )
        public String query;

        @JsonPropertyDescription(
                """
            Maximum number of relevant resume chunks to retrieve.

            Use 3 by default.
            """
        )
        public Integer limit;

        public SearchResume() {
        }
    }
    // =========================
// Tool 7: Analyze Resume Gap
// =========================

    @JsonClassDescription(
            """
        Analyzes the candidate's uploaded resume against a job description.

        Use this tool when the user asks:
        - whether they are qualified for a job
        - what skills they are missing
        - what resume gaps they have
        - how their resume compares with a job description
        - what they should improve before applying

        The analysis uses semantic resume retrieval and returns
        matched skills, partial matches, missing skills,
        evidence, gap severity, and improvement recommendations.
        """
    )
    public static class AnalyzeResumeGap {

        @JsonPropertyDescription(
                """
            The complete or relevant job description that should
            be compared against the candidate's resume.
            """
        )
        public String jobDescription;

        public AnalyzeResumeGap() {
        }
    }
    // =========================
// Tool 8: Read URL
// =========================

    @JsonClassDescription(
            """
        Reads readable text content from a public HTTP or HTTPS webpage.

        Use this tool when the user provides a URL
        and asks about the page content, such as a job posting,
        company page, article, or documentation page.
        """
    )
    public static class ReadUrl {

        @JsonPropertyDescription(
                """
            The public HTTP or HTTPS URL to read.
            """
        )
        public String url;

        public ReadUrl() {
        }
    }
    // =========================
// Tool 9: Analyze Job URL
// =========================

    @JsonClassDescription(
            """
        Reads a public job posting URL and compares the job
        requirements against the candidate's uploaded resume.

        Use this tool when the user provides a job URL and asks:
        - whether they are a good fit
        - whether they are qualified
        - what skills they are missing
        - what resume gaps exist
        - whether they should apply
        - When the user provides a job posting URL and asks whether
  they are qualified, whether the role is a good fit,
  what skills they are missing, or whether they should apply,
  use the AnalyzeJobUrl tool instead of ReadUrl.
        """
    )
    public static class AnalyzeJobUrl {

        @JsonPropertyDescription(
                """
            The public HTTP or HTTPS URL of the job posting.
            """
        )
        public String url;

        public AnalyzeJobUrl() {
        }
    }
// =========================
// Main Chat
// =========================

// =========================
// Non-streaming version
// =========================
    public ChatResult chat(
            Long conversationId,
            String message
    ) {

        return chat(
                conversationId,
                message,
                delta -> {
                    // No-op for non-streaming callers
                }
        );
    }

// =========================
// Streaming-capable version
// =========================
    public ChatResult chat(
            Long conversationId,
            String message,
            Consumer<String> deltaConsumer
    ) {

        if (message == null
                || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Chat message cannot be empty."
            );
        }

        long requestStart
                = System.nanoTime();

        System.out.println(
                "[LATENCY] ===== NEW CHAT REQUEST ====="
        );

        // =========================
        // Jev Intent Routing
        // =========================
        long jevStart
                = System.nanoTime();

        AgentIntent intent;
        boolean jevRoutingSucceeded = true;

        try {

            intent
                    = jevIntentRouter.route(
                            message
                    );

            System.out.println(
                    "[JEV INTENT] "
                    + intent
            );

        } catch (Exception e) {

            System.err.println(
                    "[JEV ROUTE FAILED] "
                    + e.getMessage()
            );

            intent
                    = AgentIntent.GENERAL;

            jevRoutingSucceeded
                    = false;
        }

        long jevMs
                = (System.nanoTime() - jevStart)
                / 1_000_000;

        System.out.println(
                "[LATENCY] Jev routing = "
                + jevMs
                + " ms"
        );

        // =========================
        // Continue Existing Chat Logic
        // =========================
        // =========================
        // Keep all your existing chat logic below this point
        // =========================
        // =========================
// Auto Generate Conversation Title
// Async - does not block chat response
// =========================
        String currentTitle
                = conversationRepository.getTitle(
                        conversationId
                );

        if (currentTitle == null
                || currentTitle.isBlank()
                || currentTitle.equalsIgnoreCase("New Chat")
                || currentTitle.equalsIgnoreCase("New Conversation")) {

            CompletableFuture.runAsync(() -> {

                try {

                    long titleStart
                            = System.nanoTime();

                    String generatedTitle
                            = conversationTitleService.generateTitle(
                                    message
                            );

                    long titleMs
                            = (System.nanoTime() - titleStart)
                            / 1_000_000;

                    System.out.println(
                            "[LATENCY] Conversation title async = "
                            + titleMs
                            + " ms"
                    );

                    conversationRepository.updateTitle(
                            conversationId,
                            generatedTitle
                    );

                    System.out.println(
                            "[CONVERSATION TITLE] "
                            + generatedTitle
                    );

                } catch (Exception e) {

                    System.err.println(
                            "[CONVERSATION TITLE ERROR] "
                            + e.getMessage()
                    );
                }
            });
        }

        // =========================
        // Load Candidate Profile
        // =========================
        long profileStart = System.nanoTime();

        CandidateProfile profile
                = profileRepository.loadProfile();

        long profileMs
                = (System.nanoTime() - profileStart)
                / 1_000_000;

        System.out.println(
                "[LATENCY] Candidate profile load = "
                + profileMs
                + " ms"
        );

        String profileContext
                = profile == null
                        ? "No candidate profile is available."
                        : profile.getProfileSummary();

        // =========================
        // Load Per
        // ====
        long historyStart = System.nanoTime();

        List<ChatMessage> recentMessages
                = chatMessageRepository.getRecentMessages(
                        conversationId,
                        10
                );

        long historyMs
                = (System.nanoTime() - historyStart)
                / 1_000_000;

        System.out.println(
                "[LATENCY] Conversation history load = "
                + historyMs
                + " ms"
        );

        StringBuilder historyBuilder
                = new StringBuilder();

        if (recentMessages.isEmpty()) {

            historyBuilder.append(
                    "No previous conversation."
            );

        } else {

            for (ChatMessage chatMessage
                    : recentMessages) {

                historyBuilder
                        .append(
                                chatMessage.getRole()
                        )
                        .append(": ")
                        .append(
                                chatMessage.getContent()
                        )
                        .append("\n");
            }
        }

        String historyContext
                = historyBuilder.toString();

        // =========================
        // Initial Prompt
        // =========================
        String prompt = """
                You are an AI career assistant
                inside a job-search application.

                You help the candidate:

                - understand job opportunities
                - evaluate job fit
                - make application decisions
                - understand their candidate profile
                - understand AI match scores
                - manage application follow-ups
                - track active job applications
                - improve job-search strategy

                Candidate Profile:

                %s

                Recent Conversation:

                %s

                Current User Message:

                %s

                Important Rules:

                - Use the recent conversation to understand
                  references such as "the second one",
                  "that job", "it", or "the previous role".

                - Use tools whenever current application
                  or database information is required.

                - Do not invent jobs, companies,
                  scores, applications, follow-ups,
                  stages, or database data.

                - If a relevant tool is available,
                  use it instead of saying that you
                  do not have access to the information.

                - When discussing one specific job ID,
                  use the job insights tool whenever
                  its stored scoring information is relevant.

                - When recommending current jobs,
                  use the top matches tool.

                - When the user asks about application follow-ups,
                use the follow-ups tool.

                - Map follow-up requests to scope values:
                "overdue" -> overdue
                "due today" / "today" -> today
                "all follow-ups" / "scheduled follow-ups" -> all
                "due" / "need attention" / "what should I follow up on" -> due

                - When the user asks which jobs they have applied to,
                  what applications they are tracking,
                  application stages,
                  or their application pipeline,
                  use the applications tool.

                - When the user asks about their skills,
                  target roles, preferred locations,
                  work preferences, experience, projects,
                  or technologies,
                  use the candidate profile tool.

                - Use candidate profile information
                  when relevant.

                - Do not invent candidate experience.

                - Clearly distinguish stored system data
                  from your interpretation of that data.

                - Answer clearly and concisely.
                - When the user asks whether their resume fits a job,
                what skills they are missing,
                what gaps exist between their resume and a job description,
                or how they should improve for a specific job,
                use the resume gap analysis tool.

                - When a full job description is provided for comparison,
                prefer the resume gap analysis tool over the general resume search tool.

                - Do not claim the candidate has a required skill unless
                the resume evidence supports it.
                """
                .formatted(
                        profileContext,
                        historyContext,
                        message
                );

        // =========================
        // V4 Task-Aware Skill Runtime
        // =========================
        TaskSelector taskSelector = new TaskSelector();

        AgentTask task = taskSelector.select(
                intent,
                message
        );

        System.out.println(
                "[AGENT TASK] " + task
        );

        try {
            SkillRuntime skillRuntime
                    = new SkillRuntime(skillSelector);

            prompt = skillRuntime.buildPrompt(
                    task,
                    prompt
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Task-aware skill runtime failed",
                    e
            );
        }

        // =========================
        // Conversation Input
        // =========================
        List<ResponseInputItem> inputs
                = new ArrayList<>();

        List<AgentTrace> toolTraces
                = new ArrayList<>();

// =========================
// Jev Routing Trace
// =========================
        if (jevRoutingSucceeded) {

            toolTraces.add(
                    new AgentTrace(
                            "JevRouter",
                            "{}",
                            "Intent: " + intent.name()
                    )
            );

        } else {

            toolTraces.add(
                    new AgentTrace(
                            "JevRouter",
                            "{}",
                            "Routing failed - fallback to all tools"
                    )
            );
        }

        ChatRequestInputFactory inputFactory
                = new ChatRequestInputFactory();

        ChatRequestInputFactory.PreparedInput preparedInput
                = inputFactory.prepare(prompt);

        inputs.add(preparedInput.input());

        // =========================
        // Register Tools
        // =========================
        ResponseCreateParams.Builder paramsBuilder
                = ResponseCreateParams
                        .builder()
                        .model(
                                ChatModel.GPT_5_2
                        )
                        .addTool(
                                GetTopMatches.class
                        )
                        .addTool(
                                GetJobInsights.class
                        )
                        .addTool(
                                GetFollowUps.class
                        )
                        .addTool(
                                GetApplications.class
                        )
                        .addTool(
                                GetCandidateProfile.class
                        )
                        .addTool(
                                SearchResume.class
                        )
                        .addTool(
                                AnalyzeResumeGap.class
                        )
                        .addTool(
                                ReadUrl.class
                        )
                        .addTool(
                                AnalyzeJobUrl.class
                        )
                        .input(
                                ResponseCreateParams.Input
                                        .ofResponse(
                                                inputs
                                        )
                        );

        // =========================
        // Multi-Tool Reasoning Loop
        // =========================
        int maxToolRounds
                = 5;

        for (int round = 1;
                round <= maxToolRounds;
                round++) {

            System.out.println(
                    "[AI REASONING ROUND] "
                    + round
            );

            paramsBuilder.input(
                    ResponseCreateParams.Input
                            .ofResponse(
                                    inputs
                            )
            );
            long aiStart = System.nanoTime();

            Response response;

            if (round == 1) {

                // =========================
                // Round 1:
                // Keep blocking so the model
                // can decide which tool to call.
                // =========================
                response = client.responses()
                        .create(
                                paramsBuilder.build()
                        );

            } else {

                // =========================
                // Round 2+:
                // Stream final answer tokens.
                // =========================
                ResponseAccumulator accumulator
                        = ResponseAccumulator.create();

                try (StreamResponse<ResponseStreamEvent> streamResponse
                        = client.responses()
                                .createStreaming(
                                        paramsBuilder.build()
                                )) {

                            streamResponse.stream()
                                    .peek(
                                            accumulator::accumulate
                                    )
                                    .flatMap(
                                            event
                                            -> event.outputTextDelta()
                                                    .stream()
                                    )
                                    .forEach(
                                            textEvent -> {

                                                String delta
                                                = textEvent.delta();

                                                System.out.print(
                                                        delta
                                                );

                                                if (deltaConsumer != null
                                                && delta != null
                                                && !delta.isEmpty()) {

                                                    deltaConsumer.accept(
                                                            delta
                                                    );
                                                }

                                                // Later we will send this
                                                // delta to SSE / frontend.
                                            }
                                    );
                        }

                        System.out.println();

                        response
                                = accumulator.response();
            }

            long aiMs
                    = (System.nanoTime() - aiStart)
                    / 1_000_000;

            System.out.println(
                    "[LATENCY] OpenAI round "
                    + round
                    + " = "
                    + aiMs
                    + " ms"
            );

            boolean toolWasCalledThisRound
                    = false;

            // =========================
// Execute Tool Calls
// =========================
            for (var item
                    : response.output()) {

                if (!item.isFunctionCall()) {

                    continue;
                }

                toolWasCalledThisRound
                        = true;

                ResponseFunctionToolCall functionCall
                        = item.asFunctionCall();

                System.out.println(
                        "[AI TOOL CALL] "
                        + functionCall.name()
                );

                System.out.println(
                        "[AI TOOL RAW ARGUMENTS] "
                        + functionCall.arguments()
                );

                // Keep tool call in reasoning context
                inputs.add(
                        ResponseInputItem
                                .ofFunctionCall(
                                        functionCall
                                )
                );

                Object toolResult;

                try {

                    long toolStart = System.nanoTime();

                    toolResult
                            = executeTool(
                                    functionCall
                            );

                    long toolMs
                            = (System.nanoTime() - toolStart)
                            / 1_000_000;

                    System.out.println(
                            "[LATENCY] Tool "
                            + functionCall.name()
                            + " = "
                            + toolMs
                            + " ms"
                    );

                } catch (Exception e) {

                    System.err.println(
                            "[AI TOOL ERROR] "
                            + functionCall.name()
                    );

                    System.err.println(
                            "[AI TOOL ERROR MESSAGE] "
                            + e.getMessage()
                    );

                    e.printStackTrace();

                    toolResult
                            = """
                Tool execution failed.

                Tool: %s

                The requested data could not be retrieved right now.

                Do not invent replacement data.
                Explain the limitation clearly to the user.
                """
                                    .formatted(
                                            functionCall.name()
                                    );
                }

                System.out.println(
                        "[AI TOOL RESULT]"
                );

                System.out.println(
                        toolResult
                );
                toolTraces.add(
                        new AgentTrace(
                                functionCall.name(),
                                functionCall.arguments().toString(),
                                String.valueOf(toolResult)
                        )
                );

                // Return result to model
                inputs.add(
                        ResponseInputItem
                                .ofFunctionCallOutput(
                                        ResponseInputItem.FunctionCallOutput
                                                .builder()
                                                .callId(
                                                        functionCall.callId()
                                                )
                                                .outputAsJson(
                                                        toolResult
                                                )
                                                .build()
                                )
                );
            }

// =========================
// Final Answer
// =========================
            if (!toolWasCalledThisRound) {

                System.out.println(
                        "[AI REASONING COMPLETE] "
                        + round
                        + " round(s)"
                );

                String finalAnswer
                        = extractOutputText(
                                response
                        );

                // =========================
                // Save Persistent Memory
                // =========================
                long persistenceStart = System.nanoTime();

                chatMessageRepository.save(
                        conversationId,
                        "user",
                        message
                );

                long assistantMessageId
                        = chatMessageRepository.save(
                                conversationId,
                                "assistant",
                                finalAnswer
                        );
                AgentToolCallRepository agentToolCallRepository
                        = new AgentToolCallRepository();

                for (AgentTrace trace : toolTraces) {

                    AgentToolCall toolCall
                            = new AgentToolCall(
                                    assistantMessageId,
                                    trace.getToolName(),
                                    trace.getArguments(),
                                    trace.getResult()
                            );

                    agentToolCallRepository.save(
                            toolCall
                    );
                }

                for (AgentTrace trace : toolTraces) {

                    AgentToolCall toolCall
                            = new AgentToolCall(
                                    assistantMessageId,
                                    trace.getToolName(),
                                    trace.getArguments(),
                                    trace.getResult()
                            );

                    agentToolCallRepository.save(
                            toolCall
                    );
                }

                long persistenceMs
                        = (System.nanoTime() - persistenceStart)
                        / 1_000_000;

                System.out.println(
                        "[LATENCY] Persistence = "
                        + persistenceMs
                        + " ms"
                );

                long totalMs
                        = (System.nanoTime() - requestStart)
                        / 1_000_000;

                System.out.println(
                        "[LATENCY] TOTAL = "
                        + totalMs
                        + " ms"
                );

                System.out.println(
                        "[LATENCY] ============================"
                );

                return new ChatResult(
                        finalAnswer,
                        toolTraces
                );
            }
        }

// =========================
// Safety Limit
// =========================
        throw new IllegalStateException(
                "AI exceeded maximum tool reasoning rounds."
        );
    }

    private Object executeTool(
            ResponseFunctionToolCall functionCall
    ) {

        return switch (functionCall.name()) {

            // =========================
            // Tool 1: Top Matches
            // =========================
            case "GetTopMatches" -> {

                GetTopMatches arguments
                        = functionCall.arguments(
                                GetTopMatches.class
                        );

                int requestedLimit
                        = arguments.limit == null
                                ? 5
                                : arguments.limit;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getTopMatches()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] limit = "
                        + requestedLimit
                );

                yield chatToolService
                .getTopMatches(
                requestedLimit
                );
            }
            // =========================
            // Tool 2: Job Insights
            // =========================
            case "GetJobInsights" -> {

                GetJobInsights arguments
                        = functionCall.arguments(
                                GetJobInsights.class
                        );

                if (arguments.jobId == null) {

                    throw new IllegalArgumentException(
                            "GetJobInsights requires jobId."
                    );
                }

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getJobInsights()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] jobId = "
                        + arguments.jobId
                );

                yield chatToolService
                .getJobInsights(
                arguments.jobId.intValue()
                );
            }

            // =========================
            // Tool 3: Follow-Ups
            // =========================
            case "GetFollowUps" -> {

                GetFollowUps arguments
                        = functionCall.arguments(
                                GetFollowUps.class
                        );

                String scope
                        = arguments.scope == null
                                ? "due"
                                : arguments.scope;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getFollowUps()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] scope = "
                        + scope
                );

                yield chatToolService
                .getFollowUps(
                scope
                );
            }

            // =========================
            // Tool 4: Applications
            // =========================
            case "GetApplications" -> {

                GetApplications arguments
                        = functionCall.arguments(
                                GetApplications.class
                        );

                String stage
                        = arguments.stage == null
                                ? "ALL"
                                : arguments.stage;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getApplications()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] stage = "
                        + stage
                );

                yield chatToolService
                .getApplications(
                stage
                );
            }

            // =========================
            // Tool 5: Candidate Profile
            // =========================
            case "GetCandidateProfile" -> {

                GetCandidateProfile arguments
                        = functionCall.arguments(
                                GetCandidateProfile.class
                        );

                String section
                        = arguments.section == null
                                ? "ALL"
                                : arguments.section;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running getCandidateProfile()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] section = "
                        + section
                );

                yield chatToolService
                .getCandidateProfile(
                section
                );
            }

            // =========================
            // Tool 6: Search Resume
            // =========================
            case "SearchResume" -> {

                SearchResume arguments
                        = functionCall.arguments(
                                SearchResume.class
                        );

                if (arguments.query == null
                        || arguments.query.isBlank()) {

                    throw new IllegalArgumentException(
                            "SearchResume requires a query."
                    );
                }

                int requestedLimit
                        = arguments.limit == null
                                ? 3
                                : arguments.limit;

                System.out.println(
                        "[AI TOOL EXECUTOR] Running searchResume()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] query = "
                        + arguments.query
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] limit = "
                        + requestedLimit
                );

                List<ResumeChunkMatch> chunks
                        = resumeRagService
                                .retrieveRelevantChunks(
                                        arguments.query,
                                        requestedLimit
                                );

                if (chunks.isEmpty()) {

                    yield "No relevant resume information was found.";
                }

                StringBuilder result
                        = new StringBuilder();

                result.append(
                        "Relevant resume evidence:\n\n"
                );

                for (int i = 0;
                        i < chunks.size();
                        i++) {

                    ResumeChunkMatch chunk
                            = chunks.get(i);

                    result.append(
                            "--- Resume Chunk "
                            + (i + 1)
                            + " ---\n"
                    );

                    result.append(
                            "Resume ID: "
                            + chunk.getResumeId()
                            + "\n"
                    );

                    result.append(
                            "Chunk Index: "
                            + chunk.getChunkIndex()
                            + "\n"
                    );

                    result.append(
                            "Similarity: "
                            + String.format(
                                    "%.4f",
                                    chunk.getSimilarity()
                            )
                            + "\n\n"
                    );

                    result.append(
                            chunk.getContent()
                    );

                    result.append(
                            "\n\n"
                    );
                }

                yield result.toString();
            }
            // =========================
// Tool 7: Analyze Resume Gap
// =========================
            case "AnalyzeResumeGap" -> {

                AnalyzeResumeGap arguments
                        = functionCall.arguments(
                                AnalyzeResumeGap.class
                        );

                if (arguments.jobDescription == null
                        || arguments.jobDescription.isBlank()) {

                    throw new IllegalArgumentException(
                            "AnalyzeResumeGap requires a job description."
                    );
                }

                System.out.println(
                        "[AI TOOL EXECUTOR] Running analyzeResumeGap()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] jobDescription length = "
                        + arguments.jobDescription.length()
                );

                ResumeGapService.GapAnalysis analysis
                        = resumeGapService.analyze(
                                arguments.jobDescription
                        );

                yield analysis.getAnalysis();
            }
            // =========================
// Tool 8: Read URL
// =========================
            case "ReadUrl" -> {

                ReadUrl arguments
                        = functionCall.arguments(
                                ReadUrl.class
                        );

                if (arguments.url == null
                        || arguments.url.isBlank()) {

                    throw new IllegalArgumentException(
                            "ReadUrl requires a URL."
                    );
                }

                System.out.println(
                        "[AI TOOL EXECUTOR] Running readUrl()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] url = "
                        + arguments.url
                );

                yield urlContentService.fetchUrl(
                arguments.url
                );
            }
            // =========================
// Tool 9: Analyze Job URL
// =========================
            case "AnalyzeJobUrl" -> {

                AnalyzeJobUrl arguments
                        = functionCall.arguments(
                                AnalyzeJobUrl.class
                        );

                if (arguments.url == null
                        || arguments.url.isBlank()) {

                    throw new IllegalArgumentException(
                            "AnalyzeJobUrl requires a URL."
                    );
                }

                System.out.println(
                        "[AI TOOL EXECUTOR] Running analyzeJobUrl()"
                );

                System.out.println(
                        "[AI TOOL ARGUMENT] url = "
                        + arguments.url
                );

                long urlStart
                        = System.nanoTime();

                String rawPageText
                        = urlContentService.fetchUrl(
                                arguments.url
                        );

                String jobDescription
                        = jobDescriptionService
                                .extractCoreJobDescription(
                                        rawPageText
                                );

                System.out.println(
                        "[JD CLEANER] Raw chars = "
                        + rawPageText.length()
                );

                System.out.println(
                        "[JD CLEANER] Clean chars = "
                        + jobDescription.length()
                );

                long urlMs
                        = (System.nanoTime() - urlStart)
                        / 1_000_000;

                System.out.println(
                        "[LATENCY] AnalyzeJobUrl URL fetch = "
                        + urlMs
                        + " ms"
                );

                long ragStart
                        = System.nanoTime();

                List<ResumeChunkMatch> chunks
                        = resumeRagService
                                .retrieveRelevantChunks(
                                        jobDescription,
                                        3
                                );

                long ragMs
                        = (System.nanoTime() - ragStart)
                        / 1_000_000;

                System.out.println(
                        "[LATENCY] AnalyzeJobUrl resume retrieval = "
                        + ragMs
                        + " ms"
                );

                StringBuilder result
                        = new StringBuilder();

                result.append(
                        "JOB DESCRIPTION:\n\n"
                );

                result.append(
                        jobDescription
                );

                result.append(
                        "\n\n"
                );

                result.append(
                        "RESUME EVIDENCE:\n\n"
                );

                if (chunks.isEmpty()) {

                    result.append(
                            "No relevant resume information was found."
                    );

                } else {

                    for (int i = 0;
                            i < chunks.size();
                            i++) {

                        ResumeChunkMatch chunk
                                = chunks.get(i);

                        result.append(
                                "--- Resume Chunk "
                                + (i + 1)
                                + " ---\n"
                        );

                        result.append(
                                "Resume ID: "
                                + chunk.getResumeId()
                                + "\n"
                        );

                        result.append(
                                "Chunk Index: "
                                + chunk.getChunkIndex()
                                + "\n"
                        );

                        result.append(
                                "Similarity: "
                                + String.format(
                                        "%.4f",
                                        chunk.getSimilarity()
                                )
                                + "\n\n"
                        );

                        result.append(
                                chunk.getContent()
                        );

                        result.append(
                                "\n\n"
                        );
                    }
                }

                yield result.toString();
            }
            // =========================
            // Unknown Tool
            // =========================
            default ->
                throw new IllegalArgumentException(
                        "Unknown tool: "
                        + functionCall.name()
                );
        };
    }

    // =========================
    // Extract Final Text
    // =========================
    private String extractOutputText(
            Response response
    ) {

        StringBuilder output
                = new StringBuilder();

        response.output()
                .stream()
                .flatMap(
                        item
                        -> item.message()
                                .stream()
                )
                .flatMap(
                        message
                        -> message.content()
                                .stream()
                )
                .flatMap(
                        content
                        -> content.outputText()
                                .stream()
                )
                .forEach(
                        text
                        -> output.append(
                                text.text()
                        )
                );

        if (output.isEmpty()) {

            throw new IllegalStateException(
                    "Could not find text in OpenAI response."
            );
        }

        return output
                .toString()
                .trim();
    }
}
